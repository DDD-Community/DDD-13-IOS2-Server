# Components — 중간지점 역 후보 추출 (MVP2)

## subway 컨텍스트 (신규)

### SubwayStation
- **Type**: Domain Model
- **Package**: com.bangawo.subway.domain
- **Purpose**: 지하철역 단일 노선 레코드 (역명 + 노선명 + 좌표)
- **Responsibilities**: 역 식별자, 역명, 노선명, 위경도 보유

### StationCandidate
- **Type**: Domain Value Object (query result)
- **Package**: com.bangawo.subway.domain
- **Purpose**: 중간지점 역 후보 계산 결과를 담는 불변 객체
- **Responsibilities**: stationName, lines(노선 합산), distanceKm 보유

### SubwayStationRepository (interface)
- **Type**: Domain Repository Interface
- **Package**: com.bangawo.subway.domain
- **Purpose**: 지하철역 공간 쿼리 추상화
- **Responsibilities**: PostGIS 기반 근거리 역 후보 조회

### SubwayStationJpaEntity
- **Type**: Infrastructure JPA Entity
- **Package**: com.bangawo.subway.infrastructure.persistence
- **Purpose**: subway_station 테이블 매핑 (PostGIS Point 포함)

### SubwayStationJpaRepository
- **Type**: Infrastructure Spring Data Repository
- **Package**: com.bangawo.subway.infrastructure.persistence
- **Purpose**: native SQL @Query 으로 PostGIS 쿼리 실행

### SubwayStationRepositoryImpl
- **Type**: Infrastructure Repository Impl
- **Package**: com.bangawo.subway.infrastructure.persistence
- **Purpose**: SubwayStationRepository 인터페이스 구현

---

## meeting 컨텍스트 (추가)

### MeetingParticipant
- **Type**: Domain Model
- **Package**: com.bangawo.meeting.domain
- **Purpose**: 모임별 참여자 스냅샷 (출발지 좌표 + 출석 상태)
- **Responsibilities**: meetingId, memberId, latitude, longitude, attendanceStatus 보유

### MeetingParticipantRepository (interface)
- **Type**: Domain Repository Interface
- **Package**: com.bangawo.meeting.domain
- **Responsibilities**: 모임별 참여자 저장/조회

### MeetingParticipantJpaEntity
- **Type**: Infrastructure JPA Entity
- **Package**: com.bangawo.meeting.infrastructure.persistence
- **Purpose**: meeting_participant 테이블 매핑 (PostGIS Point 포함)

### MeetingParticipantJpaRepository / RepositoryImpl
- **Package**: com.bangawo.meeting.infrastructure.persistence

### MidpointStationCandidate
- **Type**: Domain Model
- **Package**: com.bangawo.meeting.domain
- **Purpose**: 모임별 역 후보 저장 결과 (rank 1~3)
- **Responsibilities**: meetingId, rank, stationName, lines, distanceKm 보유

### MidpointStationCandidateRepository (interface)
- **Type**: Domain Repository Interface
- **Package**: com.bangawo.meeting.domain

### MidpointStationCandidateJpaEntity / JpaRepository / RepositoryImpl
- **Package**: com.bangawo.meeting.infrastructure.persistence

### LocationService
- **Type**: Application Service
- **Package**: com.bangawo.meeting.application
- **Purpose**: location 단계 시작 오케스트레이션 (권한 체크 + 계산 트리거 + 저장)
- **Responsibilities**: 호스트 검증, meeting 상태 전이, MidpointCalculationService 호출

### MidpointCalculationService
- **Type**: Application Service (calculation)
- **Package**: com.bangawo.meeting.application
- **Purpose**: PostGIS 기반 중간지점 역 3개 계산
- **Responsibilities**: meeting_participant 좌표 수집 → centroid → subway_station 조회 → 결과 반환

### LocationController (또는 MeetingController 확장)
- **Type**: Presentation
- **Package**: com.bangawo.meeting.presentation

### MidpointStationCandidateResponse / StationInfo
- **Type**: Presentation DTO
- **Package**: com.bangawo.meeting.presentation.dto

---

## 수정되는 기존 컴포넌트

### Meeting (domain model)
- **변경**: startLocationPhase() 메서드 추가 (locationStatus BEFORE → IN_PROGRESS)

### MeetingService 또는 CreateMeetingService
- **변경**: meeting 생성 시 MeetingParticipantRepository.saveAll() 호출 추가
