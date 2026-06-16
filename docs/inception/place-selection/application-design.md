# Application Design — 장소 선정~확정 플로우

> Inception 산출물. DDD 컨텍스트 매핑 + ERD + 도메인/파일 인벤토리 + Flyway. Branch: `feature/place-selection-flow`.

## 1. 컨텍스트 배치

- 모든 신규 도메인은 기존 **`meeting`** 컨텍스트에 배치 (장소 선정은 모임 라이프사이클의 일부, YAGNI — 신규 컨텍스트 미분리).
- `subway` 컨텍스트는 **이동부담 계산 협력자**로만 사용 (최단경로 인터페이스 추가).
- 레이어 의존: `presentation → application → domain ← infrastructure`. 도메인은 JPA/Spring 무의존.

## 2. ERD

```
                       ┌────────────────────┐
                       │      meeting        │ (기존, location_status enum 재정의)
                       │ id (PK)             │
                       └─────────┬──────────┘
                                 │ 1
        ┌────────────────────────┼───────────────────────────────────────────┐
        │ N                      │ N                  │ N            │ 1       │ N
┌───────▼────────────┐ ┌─────────▼────────┐ ┌─────────▼──────┐ ┌────▼───────┐ ┌▼──────────────────┐
│ place_recommendation│ │  place_candidate │ │   place_vote   │ │place_result│ │place_selection_   │
│ id(PK)             │ │ id(PK)           │ │ id(PK)         │ │ id(PK)     │ │  session          │
│ meeting_id(FK)     │ │ meeting_id(FK)   │ │ meeting_id(FK) │ │meeting_id  │ │ id(PK)            │
│ place_id(FK place) │ │ member_id        │ │ member_id      │ │ place_id   │ │ meeting_id(FK)    │
│ score              │ │ place_id(FK)     │ │ place_id(FK)   │ │ name       │ │ candidate_deadline│
│ station_name       │ │ created_at       │ │ created_at     │ │ address    │ │ vote_deadline     │
│ display_order      │ │ UK(meeting,      │ │ UK(meeting,    │ │confirmed_at│ │ vote_started_at   │
│ UK(meeting,place)  │ │   member,place)  │ │  member,place) │ │ UK(meeting)│ │ status            │
└────────┬───────────┘ └──────────────────┘ └────────────────┘ └────────────┘ └───────────────────┘
         │ place_id
┌────────▼──────────┐                 ┌──────────────────────────┐
│   place (V12)     │                 │  place_travel_snapshot   │  (FC-12 이동부담)
│ place_id, vibe,   │                 │ id(PK) meeting_id(FK)    │
│ category, ...     │                 │ participant_id(FK part)  │
└───────────────────┘                 │ place_id(FK place)       │
                                       │ travel_sec, transfer_cnt │
┌───────────────────┐                 │ UK(meeting,part,place)   │
│ meeting_participant│ (V11) ◄──────── └──────────────────────────┘
│ subway_station(V16)│ ◄── 최근접 역
│ subway_edge(V17)   │ ◄── 최단경로(다익스트라/BFS)
└───────────────────┘
```

### 컬럼 설계 메모
- `place_recommendation.display_order`: FC-13 4순위(등록 순서) 기준 = 추천 산출 시점 정렬 인덱스. **담긴 순서**는 `place_candidate.created_at` MIN으로 산출(요구 일치). → display_order는 추천순, 등록순서 tie-break는 candidate.created_at 사용.
- `place_vote`: 익명성 — 조회 시 member_id 절대 노출 금지(집계만). 호스트엔 완료여부만.
- `place_selection_session.status`: `CANDIDATE / VOTING / CONFIRMED / CLOSED` (DateVoteSession 패턴 일관).

## 3. 도메인 모델 / 파일 인벤토리 (신규)

### domain (`com.bangawo.meeting.domain`)
| 파일 | 비고 |
|---|---|
| `LocationStatus.java` | **수정** — `BEFORE,RECOMMENDED,VOTING,CONFIRMED` |
| `Meeting.java` | **수정** — markRecommended/startVoting/confirmPlace, computeListStatus 재작성 |
| `PlaceRecommendation.java` / `PlaceRecommendationRepository.java` | 신규 |
| `PlaceCandidate.java` / `PlaceCandidateRepository.java` | 신규 |
| `PlaceVote.java` / `PlaceVoteRepository.java` | 신규 |
| `PlaceTravelSnapshot.java` / `PlaceTravelSnapshotRepository.java` | 신규 |
| `PlaceResult.java` / `PlaceResultRepository.java` | 신규 |
| `PlaceSelectionSession.java` / `PlaceSelectionSessionRepository.java` | 신규 (마감 메타) |
| `PlaceSelectionSessionStatus.java` | 신규 enum |

### application (`com.bangawo.meeting.application`)
| 파일 | 책임 |
|---|---|
| `LocationService.java` | **수정** — startLocationPhase에 추천 산출 추가 |
| `PlaceRecommendationService.java` | FC-8 스코어링 오케스트레이션, 추천 조회 |
| `PlaceCandidateService.java` | FC-9 담기/취소/현황 + VOTING 전환 |
| `PlaceVoteService.java` | FC-11/12 투표 생성·투표·이동부담 |
| `PlaceConfirmService.java` | FC-13 우선순위 자동확정 |
| `PlaceTravelCalculationService.java` | subway_edge 최단경로 스냅샷 |
| `PlaceSelectionSchedulerService.java` | 담기/투표 마감 배치 처리 |

### subway (`com.bangawo.subway`)
| 파일 | 비고 |
|---|---|
| `domain/SubwayPathRepository.java` | 신규 — `shortestPath(fromLat,fromLng,toLat,toLng) → (travelSec, transferCount)` |
| `infrastructure/.../SubwayPathRepositoryImpl.java` | 신규 — subway_edge 인접리스트 다익스트라 |

### presentation (`com.bangawo.meeting.presentation`)
| 파일 | 비고 |
|---|---|
| `LocationController.java` | **수정** — 추천 조회 엔드포인트 추가 |
| `PlaceCandidateController.java` | 신규 |
| `PlaceVoteController.java` | 신규 |
| `PlaceResultController.java` | 신규 |
| `dto/*` | 추천/담기/투표/결과 Request·Response |

### infrastructure (`com.bangawo.meeting.infrastructure.persistence`)
- 각 도메인별 `*JpaEntity`, `*JpaRepository`, `*RepositoryImpl` (네이밍 규칙 준수).
- `infrastructure/scheduler/MeetingScheduler.java` **수정** — 마감 배치 호출 추가.

## 4. 핵심 도메인 로직

### FC-8 스코어링 (native, PostGIS)
- `place.location_point` ST_DWithin(중간역 반경) 으로 후보 필터.
- 점수 = 옵션매칭(theme/occasion 배열 교집합) + vibe 매칭 + 카테고리 일치 가중합.
- ORDER BY score DESC LIMIT 15. 각 행에 최근접 station_name 부착.

### FC-12 투표 제한
- `maxVotes = max(1, floor(candidateCount * 0.5))`.

### FC-12 이동부담 (subway_edge 그래프)
- 투표 시작 시 1회: 참여자 출발지 최근접역 → 후보 장소 최근접역 다익스트라.
- 결과 (travel_sec, transfer_count) 를 `place_travel_snapshot`에 영속.

### FC-13 우선순위 (자동확정)
```
sort candidates by:
  1) voteCount DESC
  2) SUM(travel_sec) ASC        ← place_travel_snapshot 합산
  3) SUM(transfer_count) ASC
  4) MIN(place_candidate.created_at) ASC   ← 등록(담긴) 순서
pick head → place_result, locationStatus=CONFIRMED
```

## 5. Flyway 마이그레이션 계획

| 버전 | 내용 |
|---|---|
| V18 | `location_status` enum 확장 + 데이터 매핑(`IN_PROGRESS→RECOMMENDED`,`COMPLETED→CONFIRMED`), `place_recommendation` 생성 |
| V19 | `place_candidate`, `place_selection_session` 생성 |
| V20 | `place_vote` 생성 |
| V21 | `place_travel_snapshot` 생성 |
| V22 | `place_result` 생성 |

> 한 번 적용된 스크립트 수정 금지. 실제 번호는 Construction 시점 최신 V 기준 재확인.

## 6. 신규 ErrorCode (제안)
| 코드 | status | 의미 |
|---|---|---|
| `PLACE_RECOMMENDATION_EMPTY` | 400 | 반경 내 추천 장소 0개 (미결1) |
| `PLACE_PHASE_NOT_RECOMMENDED` | 400 | RECOMMENDED 아닌데 담기 시도 |
| `PLACE_CANDIDATE_DEADLINE_PASSED` | 400 | 담기 마감 후 시도 |
| `PLACE_VOTE_NOT_IN_PROGRESS` | 400 | VOTING 아닌데 투표 |
| `PLACE_VOTE_LIMIT_EXCEEDED` | 400 | 50% 초과 투표 |
| `INVALID_VOTE_DEADLINE` | 400 | 마감일 약속일 이후/시작일 이전 |

## 7. 리스크 / 결정
- **locationStatus 재정의**가 최대 리스크 — Unit A 선행, V18 데이터 매핑 필수, `computeListStatus`/홈카드 파생 표기 회귀 테스트 필요.
- 이동부담 스냅샷 계산비용: (참여자 N × 후보 M) 다익스트라. 투표 시작 1회 + 비동기 검토(Construction).
- 미결1(후보 0개): 자동확정 강행 대신 안내 후 보류 (requirements §6 결정).
