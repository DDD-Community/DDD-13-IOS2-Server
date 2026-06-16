# Component Methods — FC-8~13

> 시그니처 레벨. 상세 비즈니스 규칙은 Functional Design(유닛별)에서 확정.

## place 컨텍스트
```java
// PlaceRepository (port)
List<RecommendationCandidate> findCandidates(
    List<Long> midpointStationIds, double radiusMeters,
    Boolean reservable, Boolean parking);   // NULL 관대(=TRUE/NULL 포함)
List<String> findDistinctVibes();

// PlaceScorer (domain service) — 순수함수
List<ScoredCandidate> score(List<RecommendationCandidate> cands,
    String themeTagCode, List<String> categories, List<String> vibes);
// score = 0.5*occ + 0.25*cat + 0.15*vibe + 0.1*rating (후보집합 min-max)

// Place
boolean matchesOccasion(String themeTagCode);   // theme_codes.contains
boolean matchesCategory(List<String> categories);
double vibeOverlap(List<String> vibes);
```

## subway 컨텍스트
```java
// SubwayEdgeRepository (port)
List<SubwayEdge> findAll();

// SubwayGraph — 부팅 시 1회 build
void load(List<SubwayEdge> edges);
PathResult shortestFrom(Long sourceStationId);   // 단일출발 다익스트라

// PathResult
long secondsTo(Long stationId);
int transfersTo(Long stationId);

// SubwayStationRepository (기존 + 추가)
Long findNearestStationId(double lat, double lng);
List<StationCandidate> findCandidatesNearMeetingCenter(Long meetingId, int limit); // 기존
```

## meeting 컨텍스트 (도메인)
```java
// Meeting
void startLocationPhase();   // guard: dateVoteStatus==COMPLETED && locationStatus==BEFORE
void toVoting();             // RECOMMENDED -> VOTING
void toConfirmed();          // VOTING -> CONFIRMED

// MeetingPlacePick
static MeetingPlacePick of(meetingId, memberId, placeId);
// MeetingPlaceVoteSession
static of(meetingId, startAt, deadline); boolean isExpired(now);
// MeetingPlaceVote
static of(sessionId, memberId, placeId);
// MeetingTravelBurden
static of(meetingId, memberId, placeId, seconds, transfers);
// MeetingConfirmedPlace
static of(meetingId, placeId, name, address);
```

## meeting 컨텍스트 (애플리케이션 서비스 — 시그니처)
```java
// PlaceSelectionService (FC-8)
void startLocationPhase(Long meetingId, Long requesterId, PlaceRecommendRequest req);
List<RecommendationView> getRecommendations(Long meetingId, Long memberId);

// PlacePickService (FC-9)
List<PlaceCardView> getPlaces(Long meetingId, Long memberId, Long stationId,
    String category, Boolean reservable, Boolean parking);
void togglePick(Long meetingId, Long memberId, Long placeId);
PickStatusView getPickStatus(Long meetingId, Long memberId);
void startVoteByHost(Long meetingId, Long hostId, VoteCreateRequest req);   // FC-9->11

// PlaceVoteService (FC-11/12)
void submitVote(Long meetingId, Long memberId, List<Long> placeIds);
VoteStatusView getVoteStatus(Long meetingId, Long memberId);   // 익명 집계 + 이동부담

// PlaceConfirmService (FC-13)
void confirm(Long meetingId);   // 4단계 순위
ConfirmedPlaceView getResult(Long meetingId, Long memberId);

// PlaceSelectionScheduler
void processPickDeadlines();   // 담기마감 -> VOTING (0개면 top3)
void processVoteDeadlines();   // 투표마감 -> confirm
```

## global
```java
// ErrorCode 신규(예)
PLACE_PHASE_NOT_READY(400)            // dateVoteStatus != COMPLETED
PLACE_RECOMMENDATION_EMPTY(400)       // 6km까지 0개
PLACE_PICK_CLOSED(400)                // 담기 마감 후 담기 시도
PLACE_VOTE_NOT_IN_PROGRESS(400)
PLACE_VOTE_LIMIT_EXCEEDED(400)        // 50% 초과
PLACE_VOTE_DEADLINE_INVALID(400)      // 마감일 < 시작 or > 약속일
```
