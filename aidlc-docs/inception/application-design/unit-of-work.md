# Unit of Work — 장소 선정~확정 (FC-8~13)

> 모놀리식 단일 서비스 내 **논리 모듈(유닛)** 분해. 분해 기준 = 의존 순서 + 바운디드 컨텍스트 + FC 경계.

## U1. 기반 — 상태/생성흐름수정/에러코드
- LocationStatus 4-state 교체 + `Meeting.startLocationPhase/toVoting/toConfirmed` 가드
- **기존 그룹/미팅 생성 흐름 수정(신규 아님)**: 미팅은 별도 생성 API 없음 — 그룹 생성 시 함께 생성됨
  - `CreateGroupRequest`(POST /groups/create) + categoryLabels/vibes
  - `CreateMeetingRequest`(다음 미팅, GroupController) + categoryLabels/vibes
  - `GroupService.createGroupWithMeeting / createNextMeeting` 시그니처 확장 → `Meeting.create` 전달
  - meeting 컬럼(categories/vibes) 저장. 그룹 테이블엔 저장 안 함
- ErrorCode 신규(장소담기/투표/마감/확정)
- Flyway: V18(meeting 컬럼), V26(locationStatus 데이터 마이그레이션)
- review 매핑: `review/fc4`(생성 수정), `review/fc8`(상태/가드)
- **선행 유닛 — 나머지 전부 의존**

## U2. 추천 — place 컨텍스트 + FC-8
- place 도메인/리포(PostGIS 후보), RecommendationCandidate, PlaceScorer, PlaceOption
- meeting_place_recommendation(V20) — place는 기존 occasion 재사용, 컬럼 변경 없음
- PlaceSelectionService(start), 추천조회, `POST location/start` 확장, `GET /places/options`
- 의존: U1

## U3. 담기 — FC-9
- MeetingPlacePick(V21), PlacePickService
- 역탭/카테고리/필터 목록 API, 담기/취소, 담기현황, 담기완료→VOTING 전환
- 담기마감 스케줄러(+3일, 0개 top3)
- 카드거리: U4 그래프 의존(또는 직선거리 임시) — **권장: U4 선행 후 통합**, 분리 시 직선거리 임시
- 의존: U2 (그래프 카드거리는 U4)

## U4. 그래프+투표 — FC-11/12
- SubwayGraph(부팅 로드) + ShortestPathService + SubwayEdgeRepository(V17 활용)
- MeetingPlaceVoteSession(V22), MeetingPlaceVote(V23), MeetingTravelBurden(V24)
- PlaceVoteService(세션생성+이동부담 스냅샷, 투표, 현황), 투표 API
- 투표마감 스케줄러
- 의존: U3 (그래프는 U2와 병렬 가능)

## U5. 확정 — FC-13
- MeetingConfirmedPlace(V25), PlaceConfirmService(4단계 순위), 결과 API
- 전원투표/마감 트리거 연결
- 의존: U4

## 코드 위치
- place → `com.bangawo.place.{domain,application,presentation,infrastructure}`
- subway 확장 → `com.bangawo.subway.{domain,infrastructure}`
- meeting 확장 → 기존 `com.bangawo.meeting.*`
- 신규 코드는 워크스페이스 루트(aidlc-docs 아님)

## 데이터 태스크 (코드와 분리, 비차단)
- vibe 표준목록 정비 — GCP 연결 시 수행 (occasion은 기존 데이터로 충분, 별도 적재 불필요)
