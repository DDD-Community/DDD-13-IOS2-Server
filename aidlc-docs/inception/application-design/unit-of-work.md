# Unit of Work — 중간지점 역 후보 추출 (MVP2)

## Unit 1: meeting_participant 도입

**목적**: 모임별 참여자 출발지 스냅샷 테이블 도입 + 모임 생성 시 자동 복사

**산출물**:
- `V11__create_meeting_participant.sql`
- `MeetingParticipant` (meeting.domain)
- `MeetingParticipantRepository` (meeting.domain)
- `MeetingParticipantJpaEntity` (meeting.infrastructure)
- `MeetingParticipantJpaRepository` (meeting.infrastructure)
- `MeetingParticipantRepositoryImpl` (meeting.infrastructure)
- `MeetingService` 또는 `CreateMeetingService` 수정 — 모임 생성 후 meeting_participant 자동 생성

**완료 기준**:
- meeting 생성 시 group_member 멤버 수만큼 meeting_participant 레코드 생성
- 각 레코드에 is_default departure_place 좌표가 PostGIS geometry로 저장
- compileJava 성공

---

## Unit 2: subway context 신규

**목적**: 지하철역 공간 데이터 도메인 + PostGIS native 쿼리 구현

**산출물**:
- `V12__create_subway_station.sql` (DDL만, GIST 인덱스 포함)
- `SubwayStation` (subway.domain)
- `StationCandidate` (subway.domain — 쿼리 결과 value object)
- `SubwayStationRepository` (subway.domain)
- `SubwayStationJpaEntity` (subway.infrastructure)
- `SubwayStationJpaRepository` (subway.infrastructure — native @Query)
- `SubwayStationRepositoryImpl` (subway.infrastructure)

**완료 기준**:
- subway_station 테이블 DDL 생성 완료
- native SQL로 centroid 계산 + 2km 이내 역 조회 쿼리 구현
- compileJava 성공

---

## Unit 3: midpoint 계산 + API

**목적**: location 단계 시작 시 역 후보 계산/저장 + 조회 API

**산출물**:
- `V13__create_midpoint_station_candidate.sql`
- `MidpointStationCandidate` (meeting.domain)
- `MidpointStationCandidateRepository` (meeting.domain)
- `MidpointStationCandidateJpaEntity` / `JpaRepository` / `RepositoryImpl` (meeting.infrastructure)
- `MidpointCalculationService` (meeting.application)
- `LocationService` (meeting.application)
- `ErrorCode` 추가 (PARTICIPANT_DEPARTURE_NOT_SET, MIDPOINT_STATION_NOT_FOUND, LOCATION_PHASE_ALREADY_STARTED)
- `Meeting.startLocationPhase()` 메서드 추가 (meeting.domain)
- `LocationController` (meeting.presentation)
- `MidpointStationCandidateResponse` (meeting.presentation.dto)

**완료 기준**:
- POST /meetings/{id}/location/start → 역 3개 저장
- GET /meetings/{id}/midpoint-stations → rank 1/2/3 반환
- 비호스트 호출 시 403
- compileJava 성공
