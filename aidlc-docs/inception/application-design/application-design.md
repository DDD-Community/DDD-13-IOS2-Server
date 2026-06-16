# Application Design (통합) — 장소 선정~확정 (FC-8~13)

> 세부: components.md / component-methods.md / services.md / component-dependency.md
> 결정 근거: requirements.md (Path B), place-selection-flow-overview.md

## 1. 설계 개요
- 신규 컨텍스트 **place**(추천), 확장 **subway**(그래프 최단경로), 확장 **meeting**(담기/투표/확정 + 상태 4-state).
- DDD 레이어/포트 준수. PostGIS 추천 + 메모리 그래프 다익스트라.

## 2. 핵심 설계 결정
| 결정 | 내용 |
|---|---|
| 상태 | locationStatus 4-state(2축 유지) + dateVoteStatus==COMPLETED 가드 |
| 추천 HARD | 반경(2→4→6km) + 예약/주차(요청, NULL 관대) |
| 추천 SOFT | 0.5·occasion + 0.25·category + 0.15·vibe + 0.1·rating |
| occasion 정합 | place.theme_codes(신규) ↔ themeTagCode, 데이터 별도 적재 |
| 모임 입력확장 | categoryLabels[], vibes[] (+ meeting 컬럼) |
| 카드/이동부담 거리 | subway_edge 그래프 단일출발 다익스트라 |
| 이동부담 저장 | VOTING 진입 시 1회 DB 스냅샷 |
| 확정 | 4단계 순위(득표→시간합→환승합→등록순) |

## 3. 컴포넌트 요약
- **place**: Place, PlaceRepository(PostGIS), RecommendationCandidate, PlaceScorer, PlaceOption
- **subway**: SubwayGraph, ShortestPathService, SubwayEdgeRepository, SubwayGraphLoader(부팅)
- **meeting**: LocationStatus(교체), Meeting(가드), MeetingPlaceRecommendation/Pick/VoteSession/Vote/TravelBurden/ConfirmedPlace, 4 서비스, Scheduler
- **global**: ErrorCode 확장

## 4. 서비스 매핑
| FC | 서비스 | 상태전이 |
|---|---|---|
| FC-8 | PlaceSelectionService | BEFORE→RECOMMENDED |
| FC-9 | PlacePickService | (전원/마감/호스트)→VOTING |
| FC-11/12 | PlaceVoteService | VOTING(세션·이동부담) |
| FC-13 | PlaceConfirmService | VOTING→CONFIRMED |
| 배치 | PlaceSelectionScheduler | 담기/투표 마감 |

## 5. 데이터 모델 (신규, V18~)
- meeting +categories/vibes (V18) / place +theme_codes (V19)
- meeting_place_recommendation(V20) / meeting_place_pick(V21)
- meeting_place_vote_session(V22) / meeting_place_vote(V23)
- meeting_travel_burden(V24) / meeting_confirmed_place(V25)
- locationStatus 데이터 마이그레이션(V26)

## 6. NFR 반영
- PostGIS 네이티브 추천, 그래프 부팅 로드(싱글턴), 단일출발 다익스트라로 비용 최소화
- 이동부담 DB 스냅샷(Cloud Run 다중 인스턴스 안전), 스케줄러 배치
- Security 베이스라인 ON, 순수 도메인 서비스 PBT(Partial) 대상

## 7. 검증
- 일관성: 컴포넌트↔메서드↔서비스↔의존성 4문서 상호 정합 확인 완료
- 미해결(데이터 태스크): place.theme_codes 적재, vibe 표준목록 — Construction 데이터 단계
