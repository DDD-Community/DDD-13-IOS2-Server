# Requirements — 장소 선정~확정 플로우 (FC-8~13)

## Intent Analysis
- **User Request**: PRD `docs/prd/mvp3.md`(MVP2+3) 기능 — 중간지역 산출 → 장소 추천 → 담기 → 투표 → 자동 확정
- **Request Type**: New Feature (브라운필드 확장)
- **Scope**: Multiple Components (meeting 확장 + place 신규 + subway_edge 그래프 신규)
- **Complexity**: Complex (PostGIS 추천 + 그래프 최단경로 + 다단계 상태/스케줄러)
- **선행 자산 재사용**: meeting_participant(V11), subway_station(V16), midpoint_station_candidate(V13), place(V12), subway_edge(V17)

---

## 확정 결정 사항 (Round 1·2 답변)

| # | 항목 | 결정 |
|---|---|---|
| 진행범위 | FC-8~13 한 사이클, AI가 context 단위로 유닛 분할 | 확정 |
| 알림 | Push/In-App 전체 **범위 제외** | 확정 |
| 상태설계 | **2축 유지** — `dateVoteStatus`(기존) + `locationStatus` 신규 4-state | A |
| locationStatus | `BEFORE → RECOMMENDED → VOTING → CONFIRMED` 로 enum 교체 | 확정 |
| 순서 가드 | `startLocationPhase` 시 `dateVoteStatus==COMPLETED` 가드 **추가** | 확정 |
| 플로우 | PRD대로 — 호스트 수동 "장소 정하기" + **담기(FC-9) 단계 유지** | A |
| 추천 거리 | FC-8 랭킹 = **직선거리(PostGIS) 집계** (장소 수백 개라 그래프 과함) | 확정 |
| 카드/이동부담 거리 | **subway_edge 그래프 최단경로**(단일출발 다익스트라) — 소수 후보에만 | 확정 |
| 환승 정의 | 최단경로상 **TRANSFER 엣지 통과 수** | 확정 |
| 탐색 반경 | 기본 2km → 부족 시 **4km → 6km 사다리(최대 6km)**, 초과 시 400 | 확정 |
| 가중치/정규화 | min-max 정규화, 수치는 **설계 단계에서 사용자 확인 후 확정** | 보류(설계) |
| 담기 마감 0개 | 스코어 **top-3 자동 후보등록** 후 VOTING (0개일 때만) | 확정 |
| 자동전환 투표마감 | 기본 **+3일** | 확정 |
| 역별 편중 | 점수순 그대로, 최소보장 없음 | 확정 |
| 투표 다중제한 | 1인 최대 = 후보수 50% 내림(최소 1) | 확정 |
| 익명성 | member_id 저장하되 집계·완료여부만 노출 | 확정 |
| 이동부담 저장 | 투표 시작 시 1회 계산 → **DB 스냅샷 테이블** (추후 캐시 고도화) | A |
| FC-9 역탭 | **유지** (장소→최근접역 귀속 라벨 필요) | A |

---

## 상태 모델

3개 독립 축 (orthogonal):
- `MeetingStatus`: ACTIVE / CLOSED
- `dateVoteStatus`: BEFORE → IN_PROGRESS → COMPLETED (기존, 변경 없음)
- `locationStatus`: **BEFORE → RECOMMENDED → VOTING → CONFIRMED** (신규 4-state)

장소축 시작 가드: `dateVoteStatus == COMPLETED` 이고 `locationStatus == BEFORE` 일 때만 호스트가 시작.

---

## Functional Requirements

### FR-8 (FC-8) 중간지역 산출 + 장소 추천
- FR-8.1 호스트만, `dateVoteStatus=COMPLETED` & `locationStatus=BEFORE`에서 시작
- FR-8.2 ATTEND/LATE 참여자 출발지 스냅샷 검증(미등록 시 400)
- FR-8.3 참여자 centroid 기준 중간지점 역 3개(rank1~3), 반경 2→4→6km 사다리
- FR-8.4 3개 역 반경 N km(기본2, 부족 시 4·6km 확대) 내 place 수집
  - **HARD 필터 = 반경 + 예약/주차(요청 시, NULL 관대: TRUE·NULL 포함, FALSE 제외)만.** (데이터 ~2천건이라 occasion 하드 제외)
- FR-8.5 **SOFT 스코어링** = `0.5·occasion + 0.25·category + 0.15·vibe + 0.1·rating` → 상위 15 (거리 제외)
  - occasion = `place.theme_codes` 가 모임 `themeTagCode` 포함 ? 1 : 0
  - category = `place.category_label` ∈ 모임 `categoryLabels` ? 1 : 0
  - vibe = |모임 `vibes` ∩ `place.vibe`| / |모임 `vibes`|
  - rating = 후보집합 min-max 정규화(없으면 0.5)
- FR-8.6 각 장소를 3개 역 중 최근접역에 귀속(역 탭 필터용)
- FR-8.7 추천 15개 스냅샷 저장, `locationStatus → RECOMMENDED`
- FR-8.8 에러: 비호스트 403 / 이미시작 400 / 출발지 미등록 400 / 6km까지 역·장소 0개 400

### FR-9 (FC-9) 장소 후보 탐색 + 담기
- FR-9.1 조회: 역 탭(역별), 카테고리, 필터(예약/주차, 이동해도 유지), 스코어 내림차순
- FR-9.2 카드: 상호명, 카테고리, 도로명주소, 분위기태그≤3, **카드거리(보는사람 출발지 기준 그래프값)**, 함께담기 수
- FR-9.3 담기/취소(토글). 1개 이상 담으면 담기완료, 0개 되면 미완료
- FR-9.4 VOTING 전환: (a)전원 담기완료 (b)담기마감 +3일 배치 (c)호스트 투표생성(후보≥1)
- FR-9.5 마감 0개 시 스코어 top-3 자동등록 후 VOTING

### FR-11 (FC-11) 투표 생성 + 마감일
- FR-11.1 마감 프리셋 +1/+3/+7일(23:59:59), 자동전환 기본 +3일
- FR-11.2 검증: 마감일 < 약속일 AND 마감일 > 시작일, 위반 시 안내

### FR-12 (FC-12) 투표 진행
- FR-12.1 정렬: 투표전 가나다 / 투표후 득표순 실시간
- FR-12.2 다중제한: 1인 최대 = 후보수 50% 내림(최소1)
- FR-12.3 완료: 1개 이상 투표 시 완료, 0개 되면 미완료
- FR-12.4 익명: member_id 저장, 집계·완료여부만 노출
- FR-12.5 이동부담: subway_edge 그래프 최단경로(참여자별 단일출발 다익스트라) → 후보별 소요시간·환승수, 투표 시작 시 1회 DB 스냅샷

### FR-13 (FC-13) 자동 확정
- FR-13.1 트리거: 전원 투표완료 OR 투표마감 배치
- FR-13.2 순위: 1)득표최다 2)이동시간합 최소 3)환승합 최소 4)등록순 빠름
- FR-13.3 전원 기권 시 4순위(등록순)로 확정
- FR-13.4 `locationStatus → CONFIRMED`, 확정 장소 고정 저장

---

## Non-Functional Requirements
- NFR-1 중간지점·추천 계산은 PostGIS 네이티브 쿼리 우선(서버 메모리 연산 최소화)
- NFR-2 출발지 좌표는 장소선정 시작 시점 스냅샷 고정
- NFR-3 subway_edge는 서버 부팅 시 메모리 인접리스트 로드, 다익스트라는 단일출발 1회로 다수 목적지 처리
- NFR-4 마감/상태전환은 스케줄러(@Scheduled, 기존 MeetingScheduler 패턴) 배치 처리
- NFR-5 DDD 레이어/네이밍 규약 준수(domain↔JpaEntity 분리, Repository 인터페이스 도메인 반환)
- NFR-6 신규 Flyway는 V18~ 채번, 적용본 수정 금지
- NFR-7 Cloud Run 다중 인스턴스 안전(이동부담 스냅샷 DB 공유)

## Extension Configuration
| Extension | Enabled |
|---|---|
| Security baseline | Yes |
| Property-Based Testing | Partial (순수함수/직렬화 라운드트립) |
| TDD code generation | No |

## 범위 제외
- 장소 검색 / 지도 뷰 단독 / 호스트 단독 장소선정 / 수동입력 / 추천 재계산
- 푸시·인앱 알림 전체
- H3 히스토리, 모임 이력 조회

## 설계 단계 확인 필요 (Open for Design)
- 스코어링 가중치 w1·w2·w3 구체 수치 + 정규화 방식 → 사용자 확인 후 확정
- 신규 테이블 스키마(추천 스냅샷 / 담기 / 투표 / 이동부담 스냅샷 / 확정결과)
- subway_edge 메모리 로딩·다익스트라 컴포넌트 배치 위치

---

## 추천 설계 최종 확정 (Path B) — 2026-06-16

### 모임 생성 입력 확장
- `CreateMeetingRequest`에 `categoryLabels[]`, `vibes[]` 추가 (기존 `themeTagCode`=occasion 유지)
- `meeting` 테이블에 `categories TEXT[]`, `vibes TEXT[]` 컬럼 추가
- 옵션 선택지 제공: category=고정 11종(PRD), vibe=`place.vibe` distinct → `GET /api/v1/places/options`

### occasion ↔ theme_tag 정합 (데이터 태스크, 코드와 분리)
- `place.theme_codes TEXT[]` 신규 컬럼 (기존 `occasion` 안 깸)
- place.occasion DISTINCT 값(수십 개) → theme_tag 8코드 매핑 사전 1회 → UPDATE (또는 LLM 배치)
- 코드는 `theme_codes` 조회하게 작성, 데이터는 추후 적재(GCP 연결 시)

### 필터/점수 순서
1. HARD: 반경(2→4→6km) + 예약/주차(요청 시, NULL 관대)
2. SOFT: 위 FR-8.5 가중합 → 상위 15

### 신규 Flyway (V18~)
- V18 meeting + categories/vibes / V19 place + theme_codes
- V20 meeting_place_recommendation / V21 meeting_place_pick
- V22 meeting_place_vote_session / V23 meeting_place_vote
- V24 meeting_travel_burden / V25 meeting_confirmed_place
- V26 locationStatus 데이터 마이그레이션(IN_PROGRESS→RECOMMENDED, COMPLETED→CONFIRMED)
