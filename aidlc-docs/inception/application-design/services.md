# Services — 중간지점 역 후보 추출 (MVP2)

## LocationService
- **Layer**: Application (meeting context)
- **Transaction**: @Transactional
- **Role**: location 단계 시작 오케스트레이션
- **Flow**:
  1. Meeting 조회 → 존재 확인
  2. GroupMember 조회 → 호스트 역할 확인
  3. meeting.startLocationPhase() (상태 전이)
  4. MidpointCalculationService.calculate() 호출
  5. 결과를 MidpointStationCandidate로 변환 후 저장
  6. meeting 저장

## MidpointCalculationService
- **Layer**: Application (meeting context)
- **Transaction**: readOnly 가능 (계산만)
- **Role**: PostGIS 기반 중간지점 역 계산
- **Flow**:
  1. SubwayStationRepository.findCandidatesNearMeetingCenter(meetingId, 3)
  2. 결과가 0개면 BusinessException(MIDPOINT_STATION_NOT_FOUND)
  3. StationCandidate 리스트 반환

## CreateMeetingService (기존 수정)
- **변경점**: meeting 저장 후 MeetingParticipantRepository.saveAll() 추가
- **Flow 추가**:
  1. (기존) group 조회 + meeting 생성 + meeting 저장
  2. (신규) groupMemberRepository.findByGroupId() 조회
  3. (신규) 각 멤버의 default departure_place 좌표로 MeetingParticipant 생성
  4. (신규) meetingParticipantRepository.saveAll()

## 서비스 의존 관계
```
LocationController
    → LocationService
        → MeetingRepository
        → GroupMemberRepository
        → MidpointCalculationService
            → SubwayStationRepository (subway context)
        → MidpointStationCandidateRepository

CreateMeetingService (기존)
    → (추가) MeetingParticipantRepository
    → (추가) DeparturePlaceRepository
```
