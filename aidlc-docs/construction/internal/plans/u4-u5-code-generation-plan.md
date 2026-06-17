# U4 (FC-11/12) + U5 (FC-13) 코드 생성 플랜

> 브랜치: feature/fc11-fc13-vote-confirm

## U4 — 그래프 + 투표 (FC-11/12)

### Step 1: V22 migration — meeting_place_vote_session
- [x] V22__create_meeting_place_vote_session.sql

### Step 2: V23 migration — meeting_place_vote
- [x] V23__create_meeting_place_vote.sql

### Step 3: V24 migration — meeting_travel_burden
- [x] V24__create_meeting_travel_burden.sql

### Step 4: subway domain — SubwayEdge + SubwayEdgeRepository
- [x] subway/domain/SubwayEdge.java
- [x] subway/domain/SubwayEdgeRepository.java

### Step 5: subway domain — SubwayGraph (Dijkstra)
- [x] subway/domain/SubwayGraph.java

### Step 6: subway infra — SubwayEdge persistence
- [x] SubwayEdgeJpaEntity.java
- [x] SubwayEdgeJpaRepository.java
- [x] SubwayEdgeRepositoryImpl.java

### Step 7: subway infra — SubwayStation nearest 추가
- [x] SubwayStationRepository 인터페이스 — findNearestStationId 추가
- [x] SubwayStationJpaRepository — PostGIS nearest 쿼리
- [x] SubwayStationRepositoryImpl — 구현

### Step 8: subway infra — SubwayGraphLoader (ApplicationRunner)
- [x] subway/infrastructure/graph/SubwayGraphLoader.java

### Step 9: meeting domain — MeetingPlaceVoteSession
- [x] meeting/domain/MeetingPlaceVoteSession.java
- [x] meeting/domain/MeetingPlaceVoteSessionRepository.java

### Step 10: meeting domain — MeetingPlaceVote
- [x] meeting/domain/MeetingPlaceVote.java
- [x] meeting/domain/MeetingPlaceVoteRepository.java

### Step 11: meeting domain — MeetingTravelBurden
- [x] meeting/domain/MeetingTravelBurden.java
- [x] meeting/domain/MeetingTravelBurdenRepository.java

### Step 12: meeting infra — VoteSession persistence
- [x] MeetingPlaceVoteSessionJpaEntity.java
- [x] MeetingPlaceVoteSessionJpaRepository.java
- [x] MeetingPlaceVoteSessionRepositoryImpl.java

### Step 13: meeting infra — Vote persistence
- [x] MeetingPlaceVoteJpaEntity.java
- [x] MeetingPlaceVoteJpaRepository.java
- [x] MeetingPlaceVoteRepositoryImpl.java

### Step 14: meeting infra — TravelBurden persistence
- [x] MeetingTravelBurdenJpaEntity.java
- [x] MeetingTravelBurdenJpaRepository.java
- [x] MeetingTravelBurdenRepositoryImpl.java

### Step 15: application — PlaceVoteService
- [x] meeting/application/PlaceVoteService.java

### Step 16: PlacePickService 수정
- [x] startVoting() + checkAndAutoTransitionToVoting() PlaceVoteService 위임

### Step 17: PlacePickSchedulerService 수정
- [x] auto-transition 시 PlaceVoteService.createSessionWithDefaultDuration(3) 호출

### Step 18: presentation — PlaceVoteController + DTOs
- [x] meeting/presentation/PlaceVoteController.java
- [x] POST /{meetingId}/place-vote/submit
- [x] GET /{meetingId}/place-vote
- [x] PlaceVoteSubmitRequest, PlaceVoteStatusResponse

### Step 19: PlaceVoteSchedulerService (신규) + MeetingScheduler 수정
- [x] meeting/application/PlaceVoteSchedulerService.java
- [x] MeetingScheduler.java 수정

## U5 — 확정 (FC-13)

### Step 20: V25 migration — meeting_confirmed_place
- [x] V25__create_meeting_confirmed_place.sql

### Step 21: meeting domain — MeetingConfirmedPlace
- [x] meeting/domain/MeetingConfirmedPlace.java
- [x] meeting/domain/MeetingConfirmedPlaceRepository.java

### Step 22: meeting infra — ConfirmedPlace persistence
- [x] MeetingConfirmedPlaceJpaEntity.java
- [x] MeetingConfirmedPlaceJpaRepository.java
- [x] MeetingConfirmedPlaceRepositoryImpl.java

### Step 23: application — PlaceConfirmService (4단계 순위)
- [x] meeting/application/PlaceConfirmService.java

### Step 24: PlaceVoteService 수정 — 전원투표 시 confirmPlace 호출
- [x] submitVote() 전원 완료 체크 → PlaceConfirmService 호출

### Step 25: PlaceVoteSchedulerService 수정 — 마감 시 confirmPlace 호출
- [x] closeExpiredSessions() → PlaceConfirmService 호출

### Step 26: presentation 수정 — GET /place-result
- [x] PlaceVoteController 수정 — GET /{meetingId}/place-result
- [x] PlaceResultResponse DTO

### Step 27: Review API doc 갱신
- [x] BUILD SUCCESSFUL — 빌드 완료
