# Component Dependency — 중간지점 역 후보 추출 (MVP2)

## 레이어 의존 방향 (DDD 원칙 준수)
```
presentation → application → domain ← infrastructure
```

## 컨텍스트 간 의존
```
meeting context application layer
    → subway context domain (SubwayStationRepository interface)
```
- meeting → subway: 단방향 (subway context는 meeting을 모름)
- subway domain interface는 meeting application에서 주입받아 사용

## 신규 컴포넌트 의존 그래프

```
LocationController
    └─> LocationService (meeting.application)
            ├─> MeetingRepository (meeting.domain)
            ├─> GroupMemberRepository (group.domain)
            ├─> MidpointCalculationService (meeting.application)
            │       └─> SubwayStationRepository (subway.domain)
            │               └─ [impl] SubwayStationRepositoryImpl (subway.infra)
            │                           └─> SubwayStationJpaRepository (native SQL)
            └─> MidpointStationCandidateRepository (meeting.domain)
                    └─ [impl] MidpointStationCandidateRepositoryImpl (meeting.infra)

CreateMeetingService (기존, 확장)
    ├─> (기존) MeetingRepository
    ├─> (신규) MeetingParticipantRepository (meeting.domain)
    │           └─ [impl] MeetingParticipantRepositoryImpl (meeting.infra)
    └─> (신규) DeparturePlaceRepository (member.domain) — default 출발지 조회
```

## 패키지 구조 (신규)
```
com.bangawo
├── meeting/
│   ├── domain/
│   │   ├── MeetingParticipant.java         (신규)
│   │   ├── MeetingParticipantRepository.java (신규)
│   │   ├── MidpointStationCandidate.java   (신규)
│   │   └── MidpointStationCandidateRepository.java (신규)
│   ├── application/
│   │   ├── LocationService.java            (신규)
│   │   ├── MidpointCalculationService.java (신규)
│   │   └── MeetingService.java             (기존, 수정)
│   ├── presentation/
│   │   ├── LocationController.java         (신규)
│   │   └── dto/
│   │       └── MidpointStationCandidateResponse.java (신규)
│   └── infrastructure/persistence/
│       ├── MeetingParticipantJpaEntity.java (신규)
│       ├── MeetingParticipantJpaRepository.java (신규)
│       ├── MeetingParticipantRepositoryImpl.java (신규)
│       ├── MidpointStationCandidateJpaEntity.java (신규)
│       ├── MidpointStationCandidateJpaRepository.java (신규)
│       └── MidpointStationCandidateRepositoryImpl.java (신규)
└── subway/                                 (컨텍스트 전체 신규)
    ├── domain/
    │   ├── SubwayStation.java
    │   ├── StationCandidate.java
    │   └── SubwayStationRepository.java
    └── infrastructure/persistence/
        ├── SubwayStationJpaEntity.java
        ├── SubwayStationJpaRepository.java
        └── SubwayStationRepositoryImpl.java
```
