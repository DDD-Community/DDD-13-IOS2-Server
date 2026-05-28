# Application Design (통합) — 중간지점 역 후보 추출 (MVP2)

## 요약

신규 subway 바운디드 컨텍스트 도입 + meeting 컨텍스트 확장.
3개 Flyway 마이그레이션, 총 신규 Java 파일 약 15개 예상.

## 컨텍스트 구성

| 컨텍스트 | 신규/수정 | 주요 컴포넌트 |
|---|---|---|
| subway (신규) | 신규 | SubwayStation, StationCandidate, SubwayStationRepository |
| meeting (확장) | 신규 7 + 수정 2 | MeetingParticipant, MidpointStationCandidate, LocationService, MidpointCalculationService |

## 핵심 설계 결정

1. **PostGIS 쿼리 위치**: SubwayStationJpaRepository의 `@Query(nativeQuery=true)` — JPQL로 ST_Centroid 등 표현 불가
2. **도메인 좌표 표현**: double lat/lng (기존 Coordinate 패턴 일치), JpaEntity에서 `org.locationtech.jts.geom.Point` 변환
3. **계산 위치**: DB 레벨 (PostGIS), Java 레벨 계산 없음 — 공간 인덱스 활용 극대화
4. **MidpointCalculationService가 subway context 접근**: SubwayStationRepository 인터페이스 주입 (컨텍스트 간 DI)

## 데이터 흐름

### POST /meetings/{id}/location/start
```
LocationController
→ LocationService.startLocationPhase(meetingId, memberId)
    1. MeetingRepository.findById()
    2. GroupMemberRepository.findByGroupIdAndMemberId() → HOST 확인
    3. meeting.startLocationPhase() → LocationStatus.IN_PROGRESS
    4. MidpointCalculationService.calculate(meetingId)
        → SubwayStationRepository.findCandidatesNearMeetingCenter(meetingId, 3)
            → native SQL: meeting_participant centroid → subway_station 2km 이내 top 3
    5. candidates → MidpointStationCandidate 변환 → saveAll()
    6. MeetingRepository.save(meeting)
→ 200 OK
```

### GET /meetings/{id}/midpoint-stations
```
LocationController
→ LocationService.getMidpointStations(meetingId, memberId)
    1. GroupMemberRepository → 멤버 확인
    2. MidpointStationCandidateRepository.findByMeetingIdOrderByRank()
→ MidpointStationCandidateResponse { candidates: [...] }
```

### Meeting 생성 시 (기존 수정)
```
CreateMeetingService (또는 MeetingService)
    1. (기존) meeting 저장
    2. (신규) groupMemberRepository.findByGroupId()
    3. (신규) departurePlaceRepository.findDefaultByMemberIdIn()
    4. (신규) MeetingParticipant 생성 → saveAll()
```

## 상세 문서 참조
- [components.md](components.md)
- [component-methods.md](component-methods.md)
- [services.md](services.md)
- [component-dependency.md](component-dependency.md)
