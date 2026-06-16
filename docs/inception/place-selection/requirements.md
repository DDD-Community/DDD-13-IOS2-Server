# Requirements — 장소 선정~확정 플로우 (FC-8 ~ FC-13)

> AI-DLC Inception 산출물. PRD: `docs/prd/temp2.md`. Branch: `feature/place-selection-flow`.

## 1. Intent Analysis

| 항목 | 내용 |
|---|---|
| User Request | 날짜 확정된 모임에서 중간지역 산출 → 장소 추천 → 담기 → 투표 → 자동 확정까지 전체 백엔드 플로우 구현 |
| Request Type | New Feature (Brownfield 확장) |
| Scope Estimate | Multiple Components — `meeting` 컨텍스트 확장 + 신규 도메인 4종, `subway` 그래프 활용 |
| Complexity | Complex — 상태기계, 스코어링, 그래프 최단경로, 스케줄러, 우선순위 확정 로직 |

## 2. 기존 자산 (활용/확장 대상)

| 자산 | 버전 | 본 기능에서의 역할 |
|---|---|---|
| `meeting.location_status` | V7 | **상태 enum 재정의 대상** (아래 §6 결정 1) |
| `meeting_participant` | V11 | 출발지 스냅샷 (FC-8 입력, FC-12 이동부담 입력) |
| `place` | V12 | 장소 마스터 + 옵션/태그/좌표 (FC-8 스코어링 소스) |
| `midpoint_station_candidate` | V13 | 중간지점 역 rank 1~3 (FC-8 반경/귀속 기준) |
| `subway_station` | V16 | 역 좌표 (최근접 역 탐색) |
| `subway_edge` | V17 | 그래프 최단경로 (이동시간/환승, FC-12/13) |
| `LocationService` / `LocationController` | 기존 | FC-8 진입점 확장 |
| `MeetingScheduler` | 기존 | 담기/투표 마감 배치에 확장 |

## 3. 기능 요구사항 (FR)

### FR-8 중간지역 산출 및 장소 추천 (FC-8)
- FR-8.1 호스트만 "장소 정하기" 시작 가능. 비호스트 → 403 (`NOT_GROUP_HOST`).
- FR-8.2 `locationStatus = BEFORE` 에서만 시작. `RECOMMENDED` 이상 → 400 (`LOCATION_PHASE_ALREADY_STARTED`).
- FR-8.3 시작 시 ATTEND/LATE 참여자 출발지 스냅샷 확정 (기존 `meeting_participant` 재사용).
- FR-8.4 출발지 미등록 참여자 존재 → 400 (`PARTICIPANT_DEPARTURE_NOT_SET`).
- FR-8.5 참여자 중심점 기준 중간지점 역 최대 3개 산출(기존 `MidpointCalculationService`). 반경 내 역 0개 → 400 (`MIDPOINT_STATION_NOT_FOUND`).
- FR-8.6 3개 역 반경 내 `place` 를 모임 옵션(테마/분위기/카테고리)으로 스코어링 → 상위 15개 선정. 각 장소를 최근접 역에 귀속.
- FR-8.7 결과를 **모임별 추천 스냅샷**(신규)으로 저장 (place 15개, score, 귀속 역, 등록 순서).
- FR-8.8 후보 < 15개면 가능한 개수만 저장. 0개 처리는 §6 미결 1.
- FR-8.9 상태 전이: `BEFORE → RECOMMENDED`.

### FR-9 장소 담기 (FC-9)
- FR-9.1 모임원이 추천 15개 중 후보를 담기/취소 (신규 **장소 후보** 테이블).
- FR-9.2 한 모임원이 ≥1 담으면 '담기 완료'. 후보 0개로 줄면 '미완료' 복귀.
- FR-9.3 역 필터: 선택 역에 귀속된 추천 장소만 노출.
- FR-9.4 카테고리/예약가능/주차가능 필터. 정렬은 추천 스코어 내림차순.
- FR-9.5 담기 현황(모임원별 완료 여부, 장소별 함께담기 N) 조회.
- FR-9.6 마감 후 담기 시도 → 차단, 투표 화면 안내.

### FR-9T 투표 전환 (FC-9 → FC-11/12)
- FR-9T.1 전원 담기 완료 시 즉시 `VOTING` 전환.
- FR-9T.2 담기 마감일(날짜 확정 +3일 23:59:59) 도래 시 자동 전환 (스케줄러).
- FR-9T.3 호스트 '투표 생성하기'(후보 ≥1) 시 즉시 전환.
- FR-9T.4 전환 시 전체 알림 발송 (트리거 §3).
- FR-9T.5 마감 시 후보 0개 → §6 미결 1.

### FR-11 투표 생성/마감일 (FC-11)
- FR-11.1 마감 프리셋 +1/+3/+7일. 마감 시각 = 선택일 23:59:59.
- FR-11.2 검증: 마감일 ≤ 약속(모임)일 이전, 마감일 > 투표 시작일.
- FR-11.3 위반 시 400 (신규 `INVALID_VOTE_DEADLINE`).
- FR-11.4 자동전환으로 호스트 미설정 시 기본 프리셋 → §6 미결 2.

### FR-12 장소 투표 (FC-12)
- FR-12.1 익명 다중 투표. 신규 **장소 투표** 테이블 (member × place).
- FR-12.2 1인 최대 = floor(후보수 × 0.5), 최소 1 보장.
- FR-12.3 ≥1 투표 시 '투표 완료'. 0개로 줄면 미완료.
- FR-12.4 마감 전 추가/취소 자유.
- FR-12.5 익명성: 누가 어디 투표했는지 비공개. 호스트는 완료 여부만 조회.
- FR-12.6 이동부담: 후보별 (참여자 × 장소) 이동시간/환승 — 투표 시작 시점 1회 계산·스냅샷(`subway_edge` 최단경로). 최장 이동 구성원 식별.
- FR-12.7 전환: 전원 투표 완료 OR 투표 마감 도래(미투표=기권) → `CONFIRMED`.

### FR-13 자동 확정 (FC-13)
- FR-13.1 우선순위: ① 득표 최다 → ② 이동시간 합 최소 → ③ 환승 합 최소 → ④ 등록(담긴) 순서 빠른 것.
- FR-13.2 전원 기권(전원 0표) → ④ 등록 순서로 확정.
- FR-13.3 확정 장소를 **장소 확정 결과**(신규)에 고정 저장.
- FR-13.4 상태 전이: `VOTING → CONFIRMED`.
- FR-13.5 확정 전체 알림.

## 4. 신규 테이블 (제안 — 상세는 application-design.md ERD)

| 테이블 | 매핑 도메인 | 핵심 컬럼 | Flyway |
|---|---|---|---|
| `place_recommendation` | `PlaceRecommendation` | meeting_id, place_id, score, station_name, display_order | V18 |
| `place_candidate` | `PlaceCandidate` | meeting_id, member_id, place_id, created_at | V19 |
| `place_vote` | `PlaceVote` | meeting_id, member_id, place_id, created_at | V20 |
| `place_travel_snapshot` | `PlaceTravelSnapshot` | meeting_id, participant_id, place_id, travel_sec, transfer_count | V21 |
| `place_result` | `PlaceResult` | meeting_id, place_id, name, address, confirmed_at | V22 |

> 마감일 메타(담기/투표 deadline)는 `place_selection_session`(신규) 또는 `meeting` 컬럼 확장 중 택1 — application-design에서 결정.

## 5. 비기능 요구사항 (NFR)
- NFR-1 중간지점/추천 스코어링은 PostGIS 네이티브 쿼리 (서버 메모리 연산 최소화).
- NFR-2 출발지 좌표는 시작 시점 스냅샷 고정.
- NFR-3 이동부담은 투표 시작 시점 스냅샷 (실시간 외부 API 미사용).
- NFR-4 마감/전환/알림은 스케줄러(일 1회 cron 기존 패턴 확장).
- NFR-5 DDD 레이어 의존 방향 준수, JpaEntity ↔ 도메인 분리, `BusinessException(ErrorCode)`.
- NFR-6 Flyway append-only — 적용된 스크립트 수정 금지.

## 6. 미결/결정 사항 (DECISIONS)

| No | 항목 | 본 Inception 결정 | 근거 |
|---|---|---|---|
| 1 | 마감 시 후보 0개 | **자동 종료 보류** — 마감 자동전환을 막고 호스트에 재시작/연장 안내(에러 노출). 자동확정 강행 안 함 | PRD §5 미결1, 데이터 무결성 우선 |
| 2 | 자동전환 시 투표 마감 기본값 | **+3일 프리셋** 자동 적용 | PRD §5 미결2 기본 방향 채택 |
| 3 | `locationStatus` enum 전환 | **`BEFORE→RECOMMENDED→VOTING→CONFIRMED`로 재정의.** 기존 `IN_PROGRESS/COMPLETED` 값 마이그레이션 필요 (V18에서 data fix) | 사용자 지시 + PRD 상태모델 |
| 4 | 마감 메타 저장 위치 | application-design에서 `place_selection_session` 신규 테이블로 분리 (날짜투표 `DateVoteSession` 패턴 일관성) | 기존 패턴 일관성 |

### ⚠️ 핵심 리스크: locationStatus enum 재정의
- 기존: `LocationStatus { BEFORE, IN_PROGRESS, COMPLETED }`, `Meeting.startLocationPhase()`가 `IN_PROGRESS`로 전이, `computeListStatus()`가 `COMPLETED` 의존.
- 신규 요구: 4단계 `BEFORE/RECOMMENDED/VOTING/CONFIRMED`.
- 영향: `LocationStatus.java`, `Meeting.java`(startLocationPhase, computeListStatus), `meeting.location_status` 데이터, `MeetingListService`/`MeetingDetailService` 파생 표기.
- 권고: enum 4값 확장 + 기존 데이터 매핑(`IN_PROGRESS→RECOMMENDED`, `COMPLETED→CONFIRMED`)을 V18 마이그레이션에 포함.
