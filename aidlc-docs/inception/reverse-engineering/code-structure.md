# Code Structure (RE refresh — FC-8~13 관점, 2026-06-16)

## Build System
- Gradle / Java 17 / Spring Boot 3.4.4
- Flyway migrations: V1 ~ V17 (17개)
- Java 소스: 171 files

## DDD 구현 패턴 (신규 코드 작성 시 동일 적용)
- **도메인 모델**: `{context}/domain/`, JPA 애노테이션 없음, `@Getter @Builder`, 정적 팩토리 + 비즈니스 메서드
- **JPA 엔티티**: `{context}/infrastructure/persistence/`, `*JpaEntity`, `from(domain)` / `toDomain()`
- **Repository 인터페이스**: `{context}/domain/`, `*Repository`, 도메인 모델 반환
- **Repository 구현체**: `{context}/infrastructure/persistence/`, `*RepositoryImpl`, JpaRepository 위임
- **앱 서비스**: `{context}/application/`, `*Service`, `@Transactional`, 오케스트레이션
- **컨트롤러**: `{context}/presentation/`, `@RestController @RequestMapping("/api/v1/...")`, DTO 변환
- **예외**: `throw new BusinessException(ErrorCode.XXX)` → `GlobalExceptionHandler`
- **마이그레이션**: `V{n}__{설명}.sql`, `BIGINT GENERATED ALWAYS AS IDENTITY`, prefix 중복 금지
- **네이티브 PostGIS 쿼리**: subway 컨텍스트 RepositoryImpl 참고 (geography, ST_Distance, GIST)

## FC-8~13 관련 기존 파일 인벤토리 (수정/참조 후보)

### meeting (확장 핵심)
- `meeting/domain/Meeting.java` — 모임 애그리거트. `locationStatus`, `startLocationPhase()` 보유 / **상태 enum 확장 필요**
- `meeting/domain/LocationStatus.java` — 현 `BEFORE/IN_PROGRESS/COMPLETED` / **PRD 기준 재정의 필요**
- `meeting/domain/MeetingParticipant.java` — 출발지 스냅샷(lat/lng, attendanceStatus)
- `meeting/domain/MidpointStationCandidate.java` — 중간지점 역 rank 1~3
- `meeting/application/LocationService.java` — 장소선정 시작 오케스트레이션 / **FC-8 추천 단계 추가 지점**
- `meeting/application/MidpointCalculationService.java` — PostGIS 중간지점 계산
- `meeting/presentation/LocationController.java` — `/location/start`, `/midpoint-stations`, `/participants/me/departure`
- `meeting/application/MeetingSchedulerService.java`, `VoteSchedulerService.java` — 마감 배치 패턴(FC-9/12 재사용)
- `meeting/infrastructure/scheduler/MeetingScheduler.java` — `@Scheduled(cron "0 0 0 * * *" KST)`
- `meeting/infrastructure/persistence/*` — JpaEntity/RepositoryImpl 패턴 참조

### subway (확장 핵심)
- `subway/domain/SubwayStationRepository.java` — `findCandidatesNearMeetingCenter(meetingId, limit)`
- `subway/domain/StationCandidate.java` — record(stationName, lines, distanceKm)
- `subway/infrastructure/persistence/SubwayStationRepositoryImpl.java` — native PostGIS 쿼리
- **subway_edge: 도메인/리포지토리 코드 없음 → 신규 그래프 로딩·최단경로 컴포넌트 필요(FC-12)**

### place (테이블만 존재 — 도메인 신규 필요)
- `place`(V12) 테이블: place_id, name, category, category_label, address, lat/lng, location_point(geography),
  has_room/has_group_seat/has_parking/reservable, max_group_size, vibe[], occasion[], naver_url 등
- **place 도메인/리포지토리/추천 스코어링 코드 전부 신규**

### member / group (참조)
- `member/domain/departure/DeparturePlace.java` — 출발지 좌표 원천
- `group/domain/GroupMember.java`, `GroupMemberRole.java`(HOST), `AttendanceStatus.java`(ATTEND/LATE/ABSENT)

### global (공통)
- `global/error/ErrorCode.java` — 에러코드 (MEETING_001~013 등) / **FC-8~13 신규 코드 추가 예정**
- `global/common/Coordinate.java` — 좌표 VO

## DB Migrations (현행 V1~V17)
| 버전 | 내용 |
|---|---|
| V11 | meeting_participant |
| V12 | place (장소 마스터) |
| V13 | midpoint_station_candidate |
| V16 | subway_station |
| V17 | **subway_edge (이동 그래프, RIDE/TRANSFER, weight_sec)** |
→ FC-8~13 신규 테이블은 **V18~** 부터 채번.

## 신규 마이그레이션 후보 (RA/설계에서 확정)
- 모임별 추천 장소 15개 스냅샷(스코어·귀속역)
- 장소 담기(후보) — 모임원별 담기/취소
- 장소 투표 — 익명 다중
- 후보별 이동부담 스냅샷(소요시간·환승) — subway_edge 최단경로 결과
- 장소 확정 결과
