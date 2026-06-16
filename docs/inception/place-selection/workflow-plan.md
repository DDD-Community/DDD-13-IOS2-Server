# Workflow Plan — 장소 선정~확정 플로우

> Inception 산출물. 상태기계 + 유닛 분해 + 시퀀스. Branch: `feature/place-selection-flow`.

## 1. 상태 머신 (locationStatus)

```
            host "장소 정하기"               전원담기 / 마감+3d / 호스트 투표생성              전원투표 / 투표마감
 BEFORE ───────────────────────► RECOMMENDED ──────────────────────────────────► VOTING ─────────────────────► CONFIRMED
   (FC-8 진입 가드)                    (FC-9 담기)                                       (FC-11/12 투표)              (FC-13 확정)
```

| From | To | Trigger | 가드 | 부수효과 |
|---|---|---|---|---|
| BEFORE | RECOMMENDED | host start | host권한, 출발지 전원보유, 반경내 역≥1 | 참여자 스냅샷, 중간역3, place_recommendation 15 |
| RECOMMENDED | VOTING | 전원 담기완료 | 후보≥1 (모든 모임원) | session 생성(+3d), 전체알림 |
| RECOMMENDED | VOTING | 담기마감 도래(+3d) | 후보≥1 (전체합) | 자동전환, 전체알림 (후보0 → 미결1) |
| RECOMMENDED | VOTING | host '투표생성' | 후보≥1 | 즉시전환, 마감 프리셋 적용, 전체알림 |
| VOTING | CONFIRMED | 전원 투표완료 | — | 우선순위 확정, place_result 저장, 알림 |
| VOTING | CONFIRMED | 투표마감 도래 | — | 미투표 기권, 확정, 알림 |

## 2. 유닛 분해 (Construction 입력)

### Unit A — locationStatus 상태기계 재정의 + 마이그레이션
- `LocationStatus` 4값 확장, `Meeting.startLocationPhase/markRecommended/startVoting/confirm`.
- `Meeting.computeListStatus` 재작성.
- V18: location_status 데이터 매핑 (`IN_PROGRESS→RECOMMENDED`, `COMPLETED→CONFIRMED`).
- **선행 유닛** — 나머지 전부 의존.

### Unit B — FC-8 장소 추천 (place_recommendation)
- `PlaceRecommendation` 도메인/리포지토리/JpaEntity.
- 스코어링 native 쿼리(`place` × 모임옵션 × 중간역 반경), 상위15 + 역귀속 + display_order.
- `LocationService.startLocationPhase` 확장 (추천 저장 + RECOMMENDED 전이).
- 추천 조회 API (역/카테고리/필터/정렬).
- V18(테이블) + 의존: Unit A, `subway_station`, `place`, `midpoint_station_candidate`.

### Unit C — FC-9 담기 (place_candidate) + 전환
- `PlaceCandidate` 도메인/리포/엔티티, 담기/취소.
- 담기 현황 조회(모임원별 완료, 장소별 함께담기 N).
- 전원완료 즉시전환 + 호스트 '투표생성' 전환.
- `place_selection_session`(담기/투표 deadline 메타) 신규.
- V19 + 의존: Unit A, B.

### Unit D — FC-11/12 투표 (place_vote) + 이동부담 스냅샷
- `PlaceVote` 도메인/리포/엔티티, 익명 다중투표(최대 50%).
- `PlaceTravelSnapshot` — 투표시작 시 (참여자×후보) 최단경로(`subway_edge`) 1회 계산·저장.
- 투표 현황/이동부담 조회, 최장이동 식별.
- 마감일 검증(FC-11).
- V20/V21 + 의존: Unit A, C, `subway_edge`.

### Unit E — FC-13 자동확정 (place_result) + 스케줄러
- 우선순위 확정 로직(득표→이동합→환승합→등록순서).
- `PlaceResult` 저장, CONFIRMED 전이.
- `MeetingScheduler` 확장: 담기마감/투표마감 배치.
- 알림 트리거 7종 연동.
- V22 + 의존: Unit A, B, C, D.

### 의존 그래프
```
A ──► B ──► C ──► D ──► E
      └─────┴─────┴──────┘  (모두 A 선행)
```

## 3. API 요약 (presentation)

| Method | Path | FC | 설명 |
|---|---|---|---|
| POST | `/api/v1/meetings/{id}/location/start` | 8 | (기존확장) 추천 산출 + RECOMMENDED |
| GET | `/api/v1/meetings/{id}/place-recommendations` | 9 | 추천 리스트(역/카테고리/필터/정렬) |
| GET | `/api/v1/meetings/{id}/places/{placeId}` | 9-3 | 장소 상세 + 함께담기 N |
| POST | `/api/v1/meetings/{id}/place-candidates` | 9 | 담기 (body: placeId) |
| DELETE | `/api/v1/meetings/{id}/place-candidates/{placeId}` | 9 | 담기 취소 |
| GET | `/api/v1/meetings/{id}/place-candidates/status` | 9-4 | 담기 현황 |
| POST | `/api/v1/meetings/{id}/place-vote/start` | 11 | (host) 투표 생성 + 마감일 |
| GET | `/api/v1/meetings/{id}/place-votes` | 12 | 투표 리스트 + 이동부담 |
| POST | `/api/v1/meetings/{id}/place-votes` | 12 | 투표 (body: placeId) |
| DELETE | `/api/v1/meetings/{id}/place-votes/{placeId}` | 12 | 투표 취소 |
| GET | `/api/v1/meetings/{id}/place-result` | 13 | 확정 결과 |

## 4. 시퀀스 (해피패스)

```
Host                 LocationService            DB/PostGIS              Scheduler
 │  start            │                          │                      │
 ├──────────────────►│ snapshot participants    │                      │
 │                   ├─────────────────────────►│                      │
 │                   │ midpoint 3 + score 15    │                      │
 │                   ├─────────────────────────►│ place_recommendation │
 │  RECOMMENDED ◄────┤                          │                      │
Member               │                          │                      │
 ├─ POST candidate ─►│ place_candidate insert   │                      │
 │ (전원완료 감지)    ├─► VOTING + session(+3d) + notify                 │
 │                   │                          │   (또는 마감배치)─────┤
Member               │                          │                      │
 ├─ POST vote ──────►│ place_vote insert        │                      │
 │ (전원투표 감지)    ├─► confirm(우선순위) ─► place_result ─► CONFIRMED + notify
 │                   │                          │   (또는 마감배치)─────┤
```

## 5. 스케줄러 (기존 `MeetingScheduler` 일 1회 cron 확장)
- 담기마감(`place_selection_session.candidate_deadline < now`, RECOMMENDED) → VOTING 전환 + 알림.
- 투표마감(`vote_deadline < now`, VOTING) → 자동확정 + 알림.
- 마감 24h/1d 전 리마인드 알림.
