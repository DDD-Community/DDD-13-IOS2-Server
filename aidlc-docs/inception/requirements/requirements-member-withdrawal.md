# 요구사항 정의 — 회원 탈퇴 기능

- 작성일: 2026-08-24
- 근거: `requirement-verification-questions-member-withdrawal.md` (Q1~Q21 전체 답변 완료)
- 관련 방침: 개인정보 수집·이용 동의표 (사용자 제공, 2026-08-24)

---

## 1. Intent Analysis

| 항목 | 내용 |
|---|---|
| 사용자 요청 | "회원 탈퇴 기능을 만들어야 한다" |
| 요청 유형 | New Feature (신규 엔드포인트 + 개인정보 파기 로직) |
| 스코프 | Multiple Components — auth / member / group / meeting / storage 컨텍스트 |
| 복잡도 | Moderate — 단일 API지만 10개 테이블 파기 정책 + 호스트 승계 + 외부 연동 해제 포함 |
| 프로젝트 유형 | Brownfield |

---

## 2. 배경 및 제약

### 2-1. 이미 존재하는 자산 (재사용)

| 자산 | 상태 |
|---|---|
| `MemberStatus.WITHDRAWN` | enum 정의 완료, 전이 로직만 없음 |
| `member.deleted_at` | 컬럼 존재(V2), 미사용 |
| `Member.isActive()` | 구현 완료 |
| 탈퇴 회원 표시 가드 | `GroupService:131`, `MeetingDetailService:75`, `MeetingListService:100`, `DateVoteService:174` 4곳에 `active ? nickname : null` 이미 적용 |
| 좌표 null 내성 | V15에서 좌표 nullable 전환 시 확보됨 (아래 2-2 참조) |

→ **Flyway 마이그레이션 불필요.** 기존 컬럼만으로 구현 가능.

### 2-2. 좌표 NULL 처리 내성 확인 결과 (중요)

탈퇴자의 출발지 좌표를 NULL로 만들어도 **기존 계산 로직은 수정 없이 정상 동작**한다. 확인 근거:

| 로직 | 위치 | NULL 내성 |
|---|---|---|
| 중간지점 산출 | `SubwayStationJpaRepository.findRawCandidatesNearMeetingCenter` | `ST_MakePoint(NULL,NULL)` → NULL, **집계함수 `ST_Collect`가 NULL을 자동 제외** → 남은 참여자 좌표로 centroid 계산. 쿼리 수정 불필요 |
| 이동부담(다익스트라) | `PlaceVoteService:486~490` | `.filter(MeetingParticipant::hasCoordinate)` 이미 적용 (`lat != null && lng != null`) |
| 거리보기 | `getPlaceTravelBurden` | 스냅샷 없는 멤버 = `seconds/transfers=null, path=[]` 처리 이미 존재 (이전 사이클 결정사항) |

**유일한 미보호 지점**: `PlaceVoteService:412` `getVoteParticipants` — `m.isActive()` 가드 없이 닉네임을 그대로 노출한다. 다른 4곳과 동일하게 수정 필요 (R8).

### 2-3. 방침상 파기 의무 (동의표 근거)

| 구분 | 보유 기간 문구 | 탈퇴 시 조치 |
|---|---|---|
| 소셜 로그인 (제공자별 회원식별정보, 인증 토큰) | 탈퇴 처리 완료 시까지 / 토큰은 탈퇴 시 파기 | 파기 |
| 회원 가입·관리 (닉네임, 서비스 인증 토큰, 약관 동의 이력) | 탈퇴 처리 완료 시까지 | 파기 |
| 출발지 (라벨·장소명·주소·위경도) | 삭제 또는 탈퇴 처리 완료 시까지 | 파기 |
| 그룹·모임 (소속·역할·투표 내역·이동 소요시간·경로) | 탈퇴 완료 또는 목적 달성 시까지. **타 이용자 권리 보호·분쟁 대응 시 최소 보관** | 원칙 파기, 예외 보관 |
| 푸시 알림 (APNs/FCM 토큰, FID) | 알림 종료·삭제요청·탈퇴 시까지 | 파기 |

---

## 3. 기능 요구사항

### R1. 탈퇴 API

- `DELETE /api/v1/members/me` — 인증 필수, 성공 시 `204 No Content`
- 탈퇴 사유는 수집하지 않는다 (Q9=A)
- Apple 계정 연동 해제를 위해 **선택 파라미터 `appleAuthorizationCode`** 를 받는다 (R7 참조)

### R2. 회원 레코드 익명화 (뼈대 유지)

단일 트랜잭션에서 `member` 행을 아래로 갱신한다.

| 컬럼 | 처리 |
|---|---|
| `status` | `WITHDRAWN` |
| `deleted_at` | `now()` |
| `nickname` | `NULL` |
| `email` | `NULL` |
| `profile_image_url` | `NULL` |
| `social_user_id` | `withdrawn_{UUID}` 로 치환 (NOT NULL 제약 유지 목적) |
| `id`, `social_provider`, `created_at` | 유지 (식별 불가, FK 무결성 유지용 뼈대) |

- 물리 삭제 배치는 두지 않는다 (Q21=A). 남는 뼈대는 개인 식별이 불가하므로 방침상 문제없음.
- 이미 `WITHDRAWN` 인 회원의 재요청은 에러로 처리한다.

### R3. 개인 소유 데이터 물리 삭제

| 테이블 | 처리 | 근거 |
|---|---|---|
| `refresh_token` | 해당 회원 행 **DELETE** | "인증 토큰은 탈퇴 시 파기" |
| `device_token` | 해당 회원 행 **DELETE** | "탈퇴 처리 시까지" |
| `departure_place` | 해당 회원 행 **DELETE** | "삭제 또는 탈퇴 처리 완료 시까지" |
| `terms_agreement` | 해당 회원 행 **DELETE** | "탈퇴 처리 완료 시까지" |

### R4. 모임 참여 데이터 — 행 유지 + 개인정보 필드 제거

| 테이블 | 처리 | 근거 |
|---|---|---|
| `meeting_participant` | 행 유지. `latitude`, `longitude`, `departure_label`, `departure_place_name`, `departure_address` → **NULL** | 출발지는 파기 대상, 참여 이력은 타 이용자 권리 보호 |
| `meeting_travel_burden` | 해당 회원 행 **DELETE** | `station_path`에 출발역이 노출되어 위치 추정 가능 → 출발지 파생 데이터 |
| `group_member` | 행 유지 (닉네임은 R2로 자동 NULL → "알 수 없음" 표시) | Q4=A |
| `date_vote_record` | 행 유지 | 투표 집계 정합성 |
| `meeting_place_pick` | 행 유지 | 투표 후보 집계 정합성 |
| `meeting_place_vote` | 행 유지 | 익명 집계, 개인 귀속 미노출 |

### R5. 프로필 이미지 파일 삭제

- 스토리지(GCS)의 프로필 이미지 객체를 삭제한다 (Q16=A)
- `profile_image_url` 이 `profiles/` 로 시작하는 경우에만 대상
- 삭제 실패가 탈퇴 트랜잭션을 롤백시키지 않는다 (best-effort, 실패 시 warn 로그)

### R6. 호스트 그룹 자동 승계

- 탈퇴자가 `HOST` 인 그룹 각각에 대해:
  - 남은 구성원(탈퇴자 제외)이 **1명 이상**이면 → `joined_at` 이 가장 이른 구성원에게 `HOST` 이전
  - 남은 구성원이 **0명**이면 → 해당 그룹을 `CLOSED` 처리
- 호스트라는 이유로 탈퇴를 차단하지 않는다.
  - 근거: App Store Review Guideline 5.1.1(v) 계정 삭제 제공 의무, 개인정보보호법 제36조 파기 요구권, 호스트 위임 API 부재로 인한 데드락 방지

### R7. Apple 연동 해제 (revoke) — **App Store 심사 필수 요건**

**근거 (Apple 공식)**
> "If your app offers Sign in with Apple, you'll need to use the Sign in with Apple REST API to revoke user tokens when deleting an account."
> — Apple Developer News, *Account deletion requirement starts June 30* (Guideline 5.1.1(v))

Apple 로그인을 제공하는 앱은 계정 삭제 시 revoke 호출이 **의무**이며 심사 시 확인 대상이다. 미구현 시 리젝 사유가 된다.

**현행 제약**: `AppleAuthClient` 는 ID Token을 디코딩만 하고 폐기하므로 서버가 보관 중인 Apple 토큰이 없다. 따라서 탈퇴 시점에 **iOS 앱이 Apple 재인증으로 새 authorization code를 발급받아 전달**해야 revoke가 성립한다.

- 소셜 제공자가 `APPLE` 인 회원이 `appleAuthorizationCode` 를 함께 보낸 경우, Apple REST API로 토큰을 폐기한다.
  1. `client_secret` 생성 — ES256 서명 JWT (Team ID / Key ID / `.p8` 개인키 / Client ID 필요)
  2. `POST https://appleid.apple.com/auth/token` — authorization code → refresh token 교환
  3. `POST https://appleid.apple.com/auth/revoke` — 토큰 폐기
- **실패해도 탈퇴는 진행한다** (best-effort, warn 로그). 외부 장애로 사용자가 탈퇴하지 못하는 상황을 만들지 않는다.
- `appleAuthorizationCode` 누락 시에도 **탈퇴는 정상 진행**한다(경고 로그). 코드 누락으로 탈퇴가 막히면 Guideline 5.1.1(v) 자체를 위반하게 되므로 차단하지 않는다.
- 카카오 unlink / 네이버 연동 해제는 이번 범위에서 제외한다(심사 의무 대상 아님).
- ⚠️ **전제조건**: Apple Developer 자격증명 4종(Team ID, Key ID, `.p8` 개인키, Client ID) 설정 필요. 미설정 시 revoke 단계는 자동 skip 되어야 한다 (§6 참조).

### R8. 탈퇴 회원 노출 차단 보완

- `PlaceVoteService.getVoteParticipants` 의 닉네임/프로필 노출에 `m.isActive()` 가드를 추가한다.
- 기존 4곳(`GroupService`, `MeetingDetailService`, `MeetingListService`, `DateVoteService`)과 동일 패턴.

### R9. 탈퇴 회원 인증 즉시 차단

- `JwtAuthenticationFilter` 에서 토큰 검증 통과 후 회원 상태를 조회하여, `ACTIVE` 가 아니면 인증을 부여하지 않는다 (401).
- 근거: Access Token 만료 1시간 → 미차단 시 탈퇴 후 최대 1시간 동안 타인의 모임 정보 조회 가능. 방침의 "탈퇴 처리 완료" 와 배치됨.
- 개별 API마다 검증을 넣는 방식(누락 위험)은 채택하지 않는다.

### R10. 재가입

- 탈퇴 회원이 동일 소셜 계정으로 재로그인하면 **신규 회원**으로 생성된다.
- `social_user_id` 가 치환되어 기존 행과 매칭되지 않으므로 `AuthService` 는 **수정 불요**.
- 과거 그룹/모임은 복구되지 않는다. 재가입 제한 기간은 두지 않는다.

---

## 4. 비기능 요구사항

| 구분 | 요구사항 |
|---|---|
| 원자성 | R2~R4(DB 변경)는 단일 트랜잭션. R5(스토리지)·R7(Apple)은 트랜잭션 외부 best-effort |
| 보안 | Security Baseline 확장 적용 (Q13=A). 본인만 자신의 계정을 탈퇴 가능 (`Authentication` principal 기준, 경로에 memberId 미노출) |
| 감사 로깅 | 탈퇴 처리 결과를 로그로 남긴다. 단, 개인 식별정보(닉네임·이메일)를 로그에 포함하지 않는다 |
| 성능 | R9로 인해 인증 요청당 회원 조회 1회 추가 (PK 인덱스 조회, 허용) |
| 테스트 | 일반 단위 테스트 (Q14=A). TDD·PBT 미적용 |
| 멱등성 | 이미 탈퇴한 회원의 재요청은 명확한 에러 코드로 응답 |

---

## 5. 스코프 제외 (Q12=A)

- 관리자용 계정 복구 기능
- 탈퇴 데이터 물리 삭제 배치(스케줄러)
- 그룹 나가기(탈퇴와 별개) 기능
- 호스트 위임 API
- 카카오 unlink / 네이버 연동 해제
- 탈퇴 사유 수집·통계

---

## 6. 전제조건 및 확인 필요 사항

> ⚠️ **아래 항목은 구현 착수를 막지 않는다.** Apple 자격증명·iOS 앱 작업 없이도 탈퇴 기능 전체를 구현·테스트할 수 있으며(미설정 시 revoke 자동 skip), 해당 항목은 App Store 심사 제출 전까지만 준비하면 된다.

| # | 항목 | 내용 |
|---|---|---|
| 1 | **Apple 자격증명** *(착수 불필요)* | R7 구현에 Team ID / Key ID / `.p8` 개인키 / Client ID 필요. 값은 환경변수·시크릿으로 주입하며 문서·명령어에 평문 기입하지 않는다. **미설정 환경에서는 revoke를 skip** 하도록 설계 |
| 2 | **클라이언트 작업** *(착수 불필요, 심사 전 필수)* | R7은 iOS 앱이 탈퇴 시 Apple 재인증 → `appleAuthorizationCode` 전달해야 성립. **서버 단독 구현 불가**, iOS 담당자와 사전 합의 필요 |
| 2-1 | **Apple ID Token 서명 검증 미구현** | `AppleAuthClient` 에 `TODO: 프로덕션에서는 Apple 공개키(JWKS)로 서명 검증 필요` 주석 존재. 탈퇴와 별개의 보안 이슈이나 심사·보안 관점에서 별도 처리 권장 (이번 스코프 외) |
| 3 | **DELETE + 요청 바디** | `DELETE /api/v1/members/me` 에 바디를 실어야 함. Spring은 지원하나 일부 클라이언트·프록시가 DELETE 바디를 누락시킬 수 있음 → **쿼리 파라미터 방식 권장**, Application Design에서 확정 |
| 4 | **약관 동의 이력 삭제** | `terms_agreement` 삭제 시 동의 증빙이 소멸. 무료 서비스로 전자상거래법상 보존 의무는 없음. 증빙 보관을 원하면 방침에 목적·기간을 **먼저 추가**해야 함 |

---

## 7. 요약

- 신규 API 1개(`DELETE /api/v1/members/me`), **Flyway 마이그레이션 없음**
- 파기: 5개 테이블 물리 삭제(`refresh_token`, `device_token`, `departure_place`, `terms_agreement`, `meeting_travel_burden`) + `member` 5개 필드 익명화 + `meeting_participant` 5개 필드 NULL + 스토리지 객체 삭제
- 유지: `group_member`, `meeting_participant`(행), `date_vote_record`, `meeting_place_pick`, `meeting_place_vote`
- 부가 변경: 호스트 자동 승계, `JwtAuthenticationFilter` 상태 검증, `getVoteParticipants` 가드 1줄, Apple revoke 클라이언트
- 기존 중간지점·이동부담 계산 로직은 **수정 불필요** (NULL 내성 기확보)
