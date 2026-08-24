# AI-DLC State Tracking

## Project Information
- **Project**: Bangawo (반가워) — 모임 조율 서비스 백엔드
- **Project Type**: Brownfield (MVP1 + MVP2(FC-4~7) 완료, 신규 MVP2+3(FC-8~13) 추가)
- **Current Feature**: 장소 선정 ~ 확정 플로우 (FC-8 ~ FC-13)
- **Feature Branch**: feature/place-selection-flow
- **Session Start**: 2026-06-16T00:00:00+09:00
- **Current Stage**: INCEPTION 완료 → CONSTRUCTION READY

## Workspace State
- **Existing Code**: Yes (Java 171 files, Spring Boot/DDD, 17 Flyway migrations)
- **Existing Contexts**: global, auth, member, group, meeting, subway, storage
- **Reverse Engineering Needed**: Yes (stale — 139→171 files, 10→17 migrations, 신규 subway/storage 컨텍스트 + place/subway_edge)
- **Workspace Root**: /c/dev/tmp/ddd/Server

## Code Location Rules
- **Application Code**: Workspace root (NEVER in aidlc-docs/)
- **Documentation**: aidlc-docs/ only
- **Structure patterns**: See code-generation.md Critical Rules

## Extension Configuration
| Extension | Enabled | Decided At |
|---|---|---|
| Security baseline | Yes | Requirements Analysis |
| Property-Based Testing | Partial | Requirements Analysis |
| TDD code generation | No | Requirements Analysis |

## GCP 배포 현황
- **Cloud Run URL**: https://bangawo-server-gzfcbbuf4q-du.a.run.app
- **Cloud SQL**: PostgreSQL 15, bangawo_prod / bangawo_user
- **배포 방식**: main 브랜치 머지 → GitHub Actions 자동 배포

## Feature Scope (FC-8 ~ FC-13)
- **PRD**: docs/prd/mvp3.md
- FC-8: 중간 지역 산출 + 장소 추천 (상위 15개, 역 귀속 태깅) — 일부 선행(midpoint V13) 존재
- FC-9: 장소 후보 리스트 탐색 및 담기 (담기/취소, 담기 완료 정의)
- FC-11: 투표 생성 및 마감일 설정 (+1/+3/+7 프리셋)
- FC-12: 장소 투표 진행 (익명 다중, 이동 부담 = subway_edge 최단경로)
- FC-13: 장소 자동 확정 (4단계 순위 로직)
- 알림: 상태 전환 스케줄러 기반 푸시/인앱

## 기존 선행 자산 (재사용)
- meeting_participant (V11), subway_station (V12/V16), midpoint_station_candidate (V13)
- place (V16), **subway_edge (V17)** — 지하철 이동그래프, 이동시간·환승 계산용 (사용자가 직접 추가)

## Stage Progress

### INCEPTION PHASE (FC-8~13)
- [x] Workspace Detection — Brownfield, 171 Java files, 17 Flyway migrations, RE stale 판정
- [x] Reverse Engineering — architecture/code-structure/api-doc/timestamp 갱신
- [x] Requirements Analysis — requirements.md 확정 (2축 유지, PRD 플로우, 거리 용도분리)
- [x] User Stories — SKIP (백엔드 API, 역할 2개 단순, PRD가 행위 상세 명세)
- [x] Workflow Planning — execution-plan.md (AD·UG·FD·NFR EXECUTE / US·Infra SKIP)
- [x] Application Design — components/methods/services/dependency/통합 5종
- [x] Units Generation — unit-of-work 3종 (5유닛, FC 매핑)
- [x] Review Artifacts — fc8·fc9·fc11·fc12·fc13 각4 + fc-group-lifecycle 보관 + fc4 수정 + overview/project-erd 갱신

### CONSTRUCTION PHASE
- [x] U1 기반 — LocationStatus 4-state, Meeting 가드, categoryLabels/vibes 입력확장, ErrorCode 8건, V18 (완료, 승인됨)
- [x] U2 추천 (FC-8) — 완료
- [x] U3 담기 (FC-9) — 완료
- [x] U4 그래프+투표 (FC-11/12)
- [x] U5 확정 (FC-13)
- [x] Build and Test — 77 tests, 0 failures
- current_unit: COMPLETE

## Previous Cycle (완료 — FC-4~7 MVP2 subway/midpoint)
- INCEPTION + CONSTRUCTION 완료 (meeting_participant, subway context, midpoint 계산 API)
- 산출물: aidlc-docs/inception/, aidlc-docs/construction/ (이전 사이클)

## New Cycle (진행중) — FC-12/13 보완 (mvp3-1 갭)
- **Feature**: 장소 투표/확정 보완 (PRD `docs/prd/mvp3-1.md` + 갭분석 `docs/prd/mvp3-1-gap-analysis.md`)
- **Type**: Brownfield 수정/확장 (기존 fc12·fc13 FC 폴더 갱신, 새 폴더 금지)
- **Session Start**: 2026-06-24
- **STEP 1 Workspace Detection**: 완료 — Brownfield, RE 아티팩트 존재·현행 유지 → RE SKIP
- **STEP 2 Reverse Engineering**: SKIP (아티팩트 존재, meeting 컨텍스트 직접 검토 완료)
- **STEP 3 Requirements Analysis**: 완료 (승인) — requirements-fc12-13-fix.md
- **STEP 4 User Stories**: SKIP (백엔드 API·역할 단순·버그픽스)
- **STEP 5 Workflow Planning**: 완료 — execution-plan-fc12-13-fix.md (AD EXECUTE, Units SKIP)
- **STEP 6 Application Design**: 완료 — application-design-fc12-13-fix.md (백필=pick+source, 순위 조회시계산, 공통 Comparator, V26)
- **STEP 7 Units Generation**: SKIP (단일 단위)
- **Review Artifacts**: 완료 — fc12·fc13 각4 + overview + project-erd 갱신 (새 폴더 미생성, 기존 FC 확장)
- **CONSTRUCTION**: 완료 — Functional Design + Code Generation(12단계) + Build&Test(94 passed, V28). R4 전원공개·R9 거리보기 스코프 반영

### 확정된 결정사항
1. 투표 후보 = **담긴 장소(pick)** 기준 (추천 15개 아님)
2. 담긴 후보 3개 미만 시 **추천 상위 순위로 백필 → 최소 3개 보장** 후 투표 시작 (0개 멈춤현상 해결)
3. submitVote **placeId 후보소속 검증** 추가
4. **멤버별 투표 참여 현황 = 전원 공개** (2026-06-24 변경: 호스트 전용→모든 구성원, 완료/미완료만 노출·투표대상 익명 유지)
5. 정렬: 투표 전 가나다순 / 투표 후 득표순
6. **수동 확정** 엔드포인트 추가 (자동 확정과 병행)
7. **1~3위 순위 산출/저장** + 확정 결과 응답 확장 (후보<3이면 후보수만큼)
8. 동점 4번째(등록순) = **그 장소 최초 담은 시각** 기준 정렬로 수정
9. 푸시 알림 / 미응답 리마인드 / 실시간 투표현황 / H3 히스토리 / 낮은반응감지 → **MVP 제외**
10. **친구들 거리보기 = 포함** (2026-06-24 변경: MVP제외→포함). 단일 장소 이동부담 조회 API 신규(R9), 스냅샷 재사용

## New Cycle (진행중) — FC-12 보완: 이동경로(station path) 스냅샷 저장
- **Feature**: 친구들 거리보기에 실제 이동경로(거쳐가는 역 좌표) 노출. 기존 FC-12 확장(새 폴더 금지).
- **Type**: Brownfield 수정/확장 (fc12 FC 폴더 갱신)
- **Session Start**: 2026-06-25
- **STEP 1 Workspace Detection**: 완료 — Brownfield, RE 아티팩트 존재·현행 유지 → RE SKIP
- **STEP 2 Reverse Engineering**: SKIP (meeting/subway 컨텍스트 직접 검토 완료)
- **STEP 3 Requirements Analysis**: 완료 — requirements-fc12-station-path.md
- **STEP 4 User Stories**: SKIP (백엔드 API·역할 단순·데이터 보강)
- **STEP 5 Workflow Planning**: SKIP (단일 단위, 직접 AD)
- **STEP 6 Application Design**: 완료 — application-design-fc12-station-path.md
- **STEP 7 Units Generation**: SKIP (단일 단위)
- **Review Artifacts**: 완료 — fc12 각4(rules/api/erd/flow) + project-erd 갱신 (새 폴더 미생성)
- **CONSTRUCTION**: 완료 — V29(station_path JSONB), dijkstra prev/경로복원, 좌표 배치조회, 스냅샷 저장+응답 노출. Build & Test 94 passed.

### 확정된 결정사항 (이번 사이클)
1. 경로 저장 = 기존 `meeting_travel_burden`에 `station_path JSONB` 컬럼(반정규화). 별도 테이블 금지(행 폭증 회피).
2. 경로 = `[{stationId, latitude, longitude}, ...]` 출발→도착 순서.
3. dijkstra가 비용+경로(prev) 동시 반환(단일 계산 재사용). 호출부 PlaceVoteService 1곳.
4. 좌표는 subway_station 배치 조회로 채움. 도달 불가 시 경로 빈 리스트.
5. 경로 노출은 친구들 거리보기(R9)에만. getVoteStatus는 요약만 유지.

## Cycle (완료) — FC-12 보완: 친구들 거리보기 응답 보강
- **Feature**: 거리보기 응답을 모임 활성 참여자 전원으로 + 출발지이름/본인여부/경로 노출. 기존 FC-12 확장.
- **Type**: Brownfield 수정/확장 (fc12 FC 폴더 갱신)
- **Session Start**: 2026-06-25
- **STEP 1~2**: SKIP (Brownfield, RE 현행 유지)
- **STEP 3 Requirements Analysis**: 완료 — requirements-fc12-travel-burden-view.md
- **STEP 4/5/7**: SKIP (백엔드 API·단일 단위)
- **STEP 6 Application Design**: 완료 — application-design-fc12-travel-burden-view.md
- **Review Artifacts**: 완료 — fc12 rules/api/flow 갱신 (스키마 변경 없음 → erd/project-erd 미변경)
- **CONSTRUCTION**: 완료 — PlaceTravelBurdenResponse 확장(departureName/isMe/nullable seconds), getPlaceTravelBurden 참여자기준 재작성 + 출발지 좌표매칭. Build & Test 94 passed.

### 확정된 결정사항 (이번 사이클)
1. 거리보기 멤버 기준 = 모임 활성 참여자(ABSENT 제외) 전원, 요청자 포함. 스냅샷 없는 멤버도 포함(seconds/transfers=null, path=[]).
2. departureName = 참여자 좌표↔DeparturePlace 매칭(placeName→label) → 기본 출발지 → null. 읽기 시점 해석, 스키마 변경 없음.
3. isMe = 요청자 본인 여부. isLongest = 소요시간 보유 멤버 중 최대.

## New Cycle (진행중) — FC-12 보완: "현재 장소 참여중인 팀원" 조회 API
- **Feature**: meetingId로 현재 투표 참여 대상 팀원 목록(이름·프로필·출발지·투표여부) 조회. 기존 FC-12 확장(새 폴더 금지).
- **Type**: Brownfield 신규 read-only 엔드포인트 (fc12 FC 폴더 갱신)
- **Session Start**: 2026-06-25
- **STEP 1 Workspace Detection**: 완료 — Brownfield, RE 아티팩트 존재·현행 유지 → RE SKIP
- **STEP 2 Reverse Engineering**: SKIP (meeting 컨텍스트 직접 검토 완료)
- **STEP 3 Requirements Analysis**: 완료 — requirements-fc12-vote-participants.md (스코프 확대: 출발지 메타 저장 + 신규 조회 API)
- **STEP 4 User Stories**: SKIP (백엔드 API·역할 단순)
- **STEP 5 Workflow Planning**: SKIP (단일 단위, 직접 AD)
- **STEP 6 Application Design**: 완료 — application-design-fc12-vote-participants.md (V30, MeetingParticipant 메타필드, 쓰기3경로, 거리보기 리팩터, 신규 getVoteParticipants)
- **STEP 7 Units Generation**: SKIP (단일 단위)
- **Review Artifacts**: 완료 — fc12 각4(rules/api/erd/flow) + project-erd 갱신 (새 폴더 미생성)
- **CONSTRUCTION**: 완료 — V30, MeetingParticipant 메타필드/departureName(), JpaEntity 매핑, 쓰기3경로(GroupService/GroupInviteService/PlaceSelectionService), getPlaceTravelBurden 좌표역매칭 제거, 신규 getVoteParticipants + VoteParticipantsResponse + Controller. Build & Test 96 passed.

### 확정된 결정사항 (이번 사이클)
1. 출발지 이름을 meeting_participant에 **직접 저장**(V30: departure_label/place_name/address). 좌표 역매칭 폐기.
2. 쓰기 3경로(GroupService/GroupInviteService/PlaceSelectionService) 모두 DeparturePlace 메타 함께 저장.
3. departureName() = placeName→label. 참여 당시 스냅샷(이후 출발지 수정 무관).
4. getPlaceTravelBurden 리팩터 — resolveDepartureName/좌표매칭 제거, 저장값 사용.
5. 신규 API GET /place-vote/participants — VOTING 필수, 활성참여자 전원, 멤버별 {name, profileImageUrl(원본key), departureName, isMe, voted}.
6. 기존 행 V30 best-effort 백필(기본 출발지 기준), 매칭 불가 시 null.
7. MeetingParticipant.create 시그니처 변경 → 기존 테스트 호출부 수정 필요.

## New Cycle (완료) — 반경 기반 주변 장소 검색 API
- **Feature**: 지도에서 반경(radius)에 따라 주변 장소를 검색하는 API
- **Type**: Brownfield 신규 엔드포인트 (기존 place 컨텍스트 확장 가능성)
- **Session Start**: 2026-06-28
- **STEP 1 Workspace Detection**: 완료 — Brownfield, 260 Java files, 31 Flyway migrations, RE 아티팩트 존재·현행 유지 → RE SKIP
- **CONSTRUCTION**: 완료 — GET /api/v1/places/nearby (commit 5bad2cb)

## New Cycle (진행중) — FC-8 보완: 장소 상세 응답 보강 (영업시간·도로명/지번·네이버링크)
- **Feature**: 장소 상세 조회 API(`GET /api/v1/places?ids=`) 응답에 영업시간·휴무·도로명/지번 주소·네이버 지도링크 추가. 기존 FC-8 확장(새 폴더 금지, fc8 갱신).
- **Type**: Brownfield 수정/확장 (place 컨텍스트 + place 테이블 컬럼 추가)
- **Session Start**: 2026-06-29
- **STEP 1 Workspace Detection**: 완료 — Brownfield, 262 Java files, 31 Flyway migrations, RE 아티팩트 존재·현행 유지 → RE SKIP
- **STEP 2 Reverse Engineering**: SKIP (아티팩트 존재, place 컨텍스트 직접 검토 완료)
- **STEP 3 Requirements Analysis**: 완료(승인) — requirements-fc8-place-detail.md (거리·함께담기N 제외, naver_url 원본, export 사용자)
- **STEP 4 User Stories**: SKIP (백엔드 API·read-only 필드 추가)
- **STEP 5 Workflow Planning**: SKIP (단일 단위)
- **STEP 6 Application Design**: 완료 — application-design-fc8-place-detail.md (V32 + 엔티티/도메인/DTO 4필드, 새 컴포넌트 없음)
- **STEP 7 Units Generation**: SKIP (단일 단위)
- **Review Artifacts**: 완료 — fc8 각4(rules/api/erd/flow) 갱신 + overview/project-erd 갱신 + PRD(mvp3 §9-3 / mvp3-1 §12-1.1) 데이터소스 주석. 승인 대기.

### 확정된 결정사항 (이번 사이클)
1. 신규 Flyway V32 — `ALTER TABLE place ADD COLUMN IF NOT EXISTS road_address/business_hours/holiday TEXT`. 기존 V12 불변.
2. `address` 의미 도로명→지번, `road_address`=도로명. (타입 동일, 데이터 재적재 시 값 교체)
3. `naver_url`은 기존 컬럼 — 응답 매핑만 신규.
4. `PlaceDetailResponse`에 `roadAddress/businessHours/holiday/naverUrl` 추가. `GET /api/v1/places?ids=`에만 적용.
5. 거리(좌표 비의존 유지)·함께담기 N(모임 맥락) 스코프 제외.
6. export 스크립트(Data/pipeline)·데이터 적재는 사용자 직접.

- **CONSTRUCTION**: 완료 — V32 마이그레이션 + Place 도메인/PlaceJpaEntity/PlaceDetailResponse에 roadAddress·businessHours·holiday·naverUrl 추가. Build & Test 94 passed, 0 failures.
- **남은 작업(사용자)**: 컬럼 생성(배포 시 V32 자동) 후 콘솔에서 데이터 적재(TRUNCATE+import 또는 UPDATE), export 스크립트 수정.

## New Cycle (진행중) — 담기 수동 종료 API + 추천 응답 장소상세 확장 (FC-9/FC-8)
- **Feature**: (1) 호스트가 담기(후보 등록)를 API로 직접 종료 — 자동종료와 동일 백필 로직. (2) 추천 응답 장소정보를 `/api/v1/places`처럼 상세화(추가 DB 조회 없이). 기존 fc9·fc8 확장(새 폴더 금지).
- **Type**: Brownfield 수정/확장 (meeting·place 컨텍스트, DTO 매핑 확장, 스키마 변경 없음)
- **Session Start**: 2026-07-08
- **STEP 1 Workspace Detection**: 완료 — Brownfield, 262 Java files, 32 Flyway migrations, RE 아티팩트 존재·현행 유지 → RE SKIP
- **STEP 2 Reverse Engineering**: SKIP (meeting/place 컨텍스트 직접 검토 완료 — 담기 종료 트리거 3종·추천 DTO 매핑 확인)
- **STEP 3 Requirements Analysis**: 완료(승인) — requirements-fc9-manual-pick-close-and-reco-detail.md
  - 확정: R1(담기 수동종료)=기존 `startVoting`(POST /place-vote)로 충족, **코드 변경 없음**(Q1'=A). 후보등록 종료=투표시작=동일 전이(RECOMMENDED→VOTING).
  - 확정: R2=`getRecommendations`의 `place`를 `PlaceSummary`→`PlaceDetailResponse` 매핑 교체. 추가 DB 조회 0건, 스키마 변경 없음.
- **STEP 4/5/7**: SKIP (백엔드 단일 단위)
- **STEP 6 Application Design**: 완료 — application-design-fc8-reco-place-detail.md (RecommendationItemResponse.place 타입 교체 + getRecommendations 매핑 1줄 + null 가드)
- **Review Artifacts**: 완료 — fc8 api.md/flow.md 갱신 + overview.md 갱신. project-erd/erd 변경 없음(스키마 무변경). fc9/fc11은 overview에 "담기종료=투표시작" 주석.

- **CONSTRUCTION**: 완료 — R2 구현: RecommendationItemResponse.place PlaceSummary→PlaceDetailResponse, PlaceSelectionService.getRecommendations 매핑 교체, PlaceDetailResponse.from null 가드. R1 코드 변경 없음. Build & Test 성공(0 failures).

### 확정된 결정사항 (회원 탈퇴 사이클)
1. 파기 방식 = soft delete(뼈대 유지) + 개인정보 즉시 파기. 물리삭제 배치 없음.
2. member 익명화 5필드 (social_user_id는 NOT NULL이라 withdrawn_{UUID} 치환) → 재가입 시 자동 신규 회원.
3. 물리삭제 4테이블: refresh_token, departure_place, terms_agreement, meeting_travel_burden.
4. meeting_participant 행 유지 + 좌표2·출발지메타3 NULL. group_member/date_vote_record/place_pick/place_vote 유지.
5. 호스트 탈퇴 차단 금지 → joined_at 최소 구성원 자동 승계, 잔여 0명이면 group CLOSED.
6. Apple revoke = App Store 5.1.1(v) 필수. X-Apple-Authorization-Code 헤더 수신. 자격증명 미설정/실패 시 skip(탈퇴는 진행).
7. JwtAuthenticationFilter에서 existsActiveById 경량조회로 탈퇴 즉시 401. @Transactional 금지, 캐시 미도입.
8. device_token = 자바코드 전무 → 이번 범위 제외, 푸시 구현 시 파기 추가 필수(문서 경고 기재).
9. terms_agreement "DELETE 금지" 주석 → 방침 우선으로 삭제 허용(주석 갱신).
10. Flyway 마이그레이션 없음.

## Phase
- phase: OPERATIONS
- stage: READY
- status: AWAITING_START
- last_updated: 2026-08-24T14:20:00Z
- note: 회원 탈퇴(FC-14) CONSTRUCTION 완료 — Code Generation + Build & Test(118 tests, 0 failures) 모두 승인됨. 운영 전환 전 확인 필요: Apple 자격증명 4종(배포 환경 시크릿), iOS의 X-Apple-Authorization-Code 헤더 전달. 둘 다 없어도 배포·운영 가능(revoke만 skip).

## New Cycle (진행중) — 회원 탈퇴 기능
- **Feature**: 회원 탈퇴(계정 삭제/비활성) API. auth/member 컨텍스트 확장.
- **Type**: Brownfield 신규 엔드포인트 + 회원 상태/연관 데이터 처리
- **Session Start**: 2026-08-24
- **STEP 1 Workspace Detection**: 완료 — Brownfield, 262 Java files, 32 Flyway migrations(V32), 컨텍스트 8종(auth/member/group/meeting/place/subway/storage/global). RE 아티팩트 존재·현행 유지 → RE SKIP
- **STEP 2 Reverse Engineering**: SKIP (auth/member 컨텍스트 직접 검토 완료 — member.status/deleted_at, MemberStatus.WITHDRAWN 이미 정의, member(id) 참조 8개 테이블 확인)
- **STEP 3 Requirements Analysis**: 완료(승인 대기) — requirements-member-withdrawal.md
- **Extension Configuration (이번 사이클)**: Security baseline=Yes, PBT=No, TDD=No
- **STEP 4 User Stories**: SKIP (백엔드 API 1개, 역할 단순)
- **STEP 5 Workflow Planning**: 완료 — execution-plan-member-withdrawal.md (AD·Review EXECUTE / RE·US·UG SKIP, 리스크 Medium)
- **STEP 6 Application Design**: 완료 — application-design-member-withdrawal.md (D1=C 헤더 / D2=B 신규 MemberWithdrawalService / D3=A 리포지토리 직접주입)
- **Review Artifacts**: 완료 — fc14 각4(rules/api/erd/flow) 신규 + overview.md 갱신(FC-14 행·회원 생명주기·권한) + project-erd.md 갱신(파기 매트릭스). 스키마 변경 없어 마이그레이션 없음
- **STEP 7 Units Generation**: SKIP (단일 단위)
- **Review 대상 FC**: fc14 (신규, 기존 최대 fc13 다음 번호)
- **CONSTRUCTION — Code Generation**: 완료 — `fc14-member-withdrawal-code-generation-plan.md` 전체 15 Step 완료. `DELETE /api/v1/members/me`, `MemberWithdrawalService`(TransactionTemplate 기반 TX 경계 분리), `AppleTokenRevoker`(+Impl, ES256 client_secret), `AppleRevokeProperties`, 파기용 리포지토리 메서드 6종, `JwtAuthenticationFilter` R9 가드, `PlaceVoteService.getVoteParticipants` R8 가드. 컴파일 성공 + 신규/변경 테스트 7클래스 35건 전체 통과. 마이그레이션 없음. 사용자 승인 완료.
- **CONSTRUCTION — Build and Test**: 완료 — `./gradlew clean build` BUILD SUCCESSFUL, 전체 118 tests / 0 failures (이전 96 → +22). 산출물: `aidlc-docs/construction/build-and-test/{build-instructions,unit-test-instructions,integration-test-instructions,build-and-test-summary}.md` 갱신.
- current_unit: fc14 (단일 단위) — COMPLETE
