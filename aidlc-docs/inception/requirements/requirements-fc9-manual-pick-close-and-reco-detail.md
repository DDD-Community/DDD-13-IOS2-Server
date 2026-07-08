# 요구사항 분석 — 담기(후보 등록) 수동 종료 API + 추천 응답 장소상세 확장

- **작성일**: 2026-07-08
- **유형**: Brownfield 수정/확장 (기존 FC-9 담기 + FC-8 추천 확장, 새 폴더 금지)
- **요청 요약**:
  1. 장소 후보 등록(담기)을 **호스트가 API로 직접 종료**할 수 있게 한다. 자동 종료(전원 완료 / 3일 마감)와 **동일한 로직**(후보 3개 미만 시 추천 상위 순위로 백필 → 최소 3개 보장) 사용.
  2. `GET /api/v1/meetings/{meetingId}/recommendations` 응답의 장소 정보를 `GET /api/v1/places`처럼 **상세 필드까지** 내려준다 — 단, **추가 DB 조회 없이 가능한 범위**에서.

---

## 1. 현행 코드 사실 확인 (Reverse Engineering 대체 — 직접 검토)

### 1-1. 담기(후보 등록) 종료 = RECOMMENDED → VOTING 전이. 현재 트리거 3종
| # | 트리거 | 코드 | 백필 | 마감기간 |
|---|---|---|---|---|
| 1 | **전원 담기 완료** (활성 참여자 전원 ≥1건 담기) | `PlacePickService.checkAndAutoTransitionToVoting` | O (`createSessionWithDefaultDuration`) | 기본(DEFAULT_DURATION_DAYS) |
| 2 | **3일 마감** (pickDeadline = 시작+3일 23:59:59) | `PlacePickSchedulerService.processExpiredPickDeadlines` (자정 스케줄러) | O (`createSessionWithDefaultDuration`) | 기본 |
| 3 | **호스트 수동 투표 시작** `POST /{meetingId}/place-vote` | `PlaceVoteService.startVoting(meetingId, memberId, durationDays)` | O (백필 최소 3개 보장) | **호스트 지정(1/3/7)** |

- 공통 백필: `MIN_CANDIDATES = 3`. 담긴 후보 < 3이면 `meeting_place_recommendation` rank 순으로 `MeetingPlacePick.ofSystem` 보충, 추천 총량 부족 시 가능한 만큼만.
- 전이 가드: `Meeting.toVoting()` — `locationStatus != RECOMMENDED`면 `LOCATION_NOT_RECOMMENDED`.

> **핵심 관찰**: 사용자가 원하는 "그 로직 그대로 API로 직접 후보등록 종료"는 이미 **트리거 3번(`startVoting`)** 이 수행하고 있음. 다만 `startVoting`은 `durationDays`(투표 마감기간)를 **호스트가 필수 지정**하고 "투표 시작"이라는 이름/의미를 가짐.

### 1-2. 추천 응답 현황
- `GET /{meetingId}/recommendations` → `getRecommendations` → `List<RecommendationItemResponse>`.
- `RecommendationItemResponse = { rank, PlaceSummary place, score, nearestStationId }`.
- `PlaceSummary` 필드: placeId, name, categoryLabel, address, latitude, longitude (요약).
- `PlaceDetailResponse`(= `/api/v1/places` 응답) 필드: 위 + roadAddress, vibe, occasion, reservable, hasParking, rating, businessHours, holiday, naverUrl (상세).
- **`getRecommendations`는 이미 `placeRepository.findByIds(...)`로 `Place` 도메인 전체를 메모리에 로딩** 후 `PlaceSummary.from()`으로 축약해 내려줌.

> **핵심 관찰**: `Place` 객체가 이미 완전히 로딩되어 있으므로 **추가 DB 조회 0건**으로 `PlaceDetailResponse.from()` 매핑만 교체하면 상세 필드 제공 가능. → 요구사항 2 **기술적으로 완전히 가능**.

---

## 2. 확정 요구사항 (초안)

### R1. 담기 수동 종료 API
- 호스트가 담기 단계(RECOMMENDED)를 즉시 종료하고 투표(VOTING)로 전환하는 엔드포인트.
- 종료 시 자동 종료와 동일하게 후보 < 3이면 추천 순위 백필 → 최소 3개 보장.
- 권한: 호스트 전용. 상태: RECOMMENDED 아니면 `LOCATION_NOT_RECOMMENDED`.

### R2. 추천 응답 장소 상세화
- `GET /{meetingId}/recommendations`의 `place` 필드를 요약(`PlaceSummary`) → 상세(`PlaceDetailResponse` 동급)로 확장.
- 추가 DB 조회 없이 이미 로딩된 `Place`에서 매핑만 확장.

---

## 3. 사용자 확인 결과 (2026-07-08 반영)

### 확정 사항
- **후보 등록 종료 = 투표 시작 = 동일한 상태 전이(`RECOMMENDED → VOTING`).** 중간 상태 없음. 별개 단계 아님.
- **R1은 기존 `POST /{meetingId}/place-vote`(`startVoting`)로 이미 충족.** 호출 즉시 후보 등록 종료 + 백필(최소 3개) + 투표 시작. → **신규 엔드포인트 개발 없음.** 프론트가 이 API를 "후보 등록 종료 후 바로 투표 시작"에 사용.
- **R2**: 추천 응답 `place`를 기존 `PlaceDetailResponse`(= `/api/v1/places` 응답 DTO) **그대로 재사용**. `PlaceSummary` 제거, 이미 로딩된 `Place` 매핑만 교체. 추가 DB 조회 0건. `rank`/`score`/`nearestStationId` 유지.
  - (Q2·Q3은 결국 같은 결정 — "기존 상세 객체 그대로 반환"으로 통합)

### 남은 미결 (1건만)
- **Q1'. `durationDays` 요청 방식** — `startVoting`은 현재 마감기간(1/3/7)을 **요청 바디로 필수** 받음.
  - **(A)** 그대로 유지 — 프론트가 durationDays 전달. (기본, 신규 개발 없음)
  - **(B)** durationDays **optional** 처리 — 미전달 시 기본기간(`DEFAULT_DURATION_DAYS`)으로 시작. (소소한 수정)
  - → **확정: (A)** — startVoting 현행 유지, durationDays 필수 그대로. R1 코드 변경 전무.

---

## 4. 스코프 / 비스코프

**In scope (확정)**
- **R2만 실제 구현**: `RecommendationItemResponse.place` 타입을 `PlaceSummary` → `PlaceDetailResponse`로 교체 + `getRecommendations` 매핑 변경. (fc8 리뷰 폴더 갱신)
- (Q1'가 B인 경우에 한해) `startVoting` durationDays optional 소소 수정.

**Out of scope**
- 담기 종료 신규 엔드포인트 (기존 `startVoting`으로 충족 — 개발 없음)
- 백필 로직 변경 (기존 재사용)
- 새 DB 컬럼/마이그레이션 (추가 조회·스키마 변경 없음)
- 상태 모델 변경 (RECOMMENDED/VOTING 그대로)

## 5. 비기능 요구
- 성능: 추가 DB 조회 0건 유지 (R2 핵심 제약).
- 보안: 기존 JWT + 그룹 구성원/호스트 인가 재사용.
- 확장: 기존 FC-8(fc8)·FC-9(fc9) 리뷰 폴더 갱신, 새 폴더 금지.
