# 비즈니스 규칙 — FC-14 회원 탈퇴

> 2026-08-24 신규. 개인정보 수집·이용 동의표(방침) 기반 파기 정책.

---

## ✅ 구현 착수 조건 — 선행 조건 없음

**지금 바로 구현 가능하다.** 아래 항목은 착수를 막지 않는다.

| 항목 | 착수에 필요? | 설명 |
|---|---|---|
| Apple 자격증명 4종 (Team ID / Key ID / `.p8` / Client ID) | **아니오** | 미설정 시 revoke를 자동 skip. 탈퇴 기능 전체가 정상 동작한다 |
| iOS 앱의 `X-Apple-Authorization-Code` 전달 | **아니오** | 헤더가 없으면 revoke를 skip. 탈퇴는 정상 진행된다 |
| Flyway 마이그레이션 | **아니오** | 스키마 변경이 없다 |

> 위 2개는 **App Store 심사 제출 전까지만** 준비하면 되는 항목이다.
> 자격증명이 준비되면 환경변수만 주입하면 되고, **코드 재작업은 발생하지 않는다.**

### 구현 범위에서 제외된 것 (나중에 추가)

| 항목 | 시점 |
|---|---|
| Apple 자격증명 실제 주입 | 심사 제출 전 |
| iOS 앱의 authorizationCode 전달 | 심사 제출 전 |
| `device_token` 파기 | 푸시 알림 기능 구현 시 |

---

## 트리거
- 사용자가 앱에서 탈퇴 요청 (`DELETE /api/v1/members/me`)
- 본인만 자신의 계정을 탈퇴 가능. 경로에 memberId 미노출(인증 principal 사용)

## 전제 조건
- 인증된 회원일 것
- 이미 `WITHDRAWN` 상태면 거부 (`MEMBER_007`)
- **호스트라는 이유로 차단하지 않는다** (App Store 5.1.1(v) 계정 삭제 의무 / 개인정보보호법 §36 파기 요구권)

---

## 파기 매트릭스 (핵심)

### A. 회원 레코드 — 뼈대만 유지

| 컬럼 | 처리 |
|---|---|
| `status` | `WITHDRAWN` |
| `deleted_at` | `now()` |
| `nickname` | `NULL` |
| `email` | `NULL` |
| `profile_image_url` | `NULL` |
| `social_user_id` | `withdrawn_{UUID}` 치환 (NOT NULL 제약 유지) |
| `id`·`social_provider`·`created_at` | 유지 (식별 불가, FK 무결성용) |

### B. 물리 삭제 (DELETE)

| 테이블 | 방침 근거 |
|---|---|
| `refresh_token` | "인증 토큰은 로그아웃·탈퇴 시 파기" |
| `departure_place` | "출발지 삭제 또는 탈퇴 처리 완료 시까지" |
| `terms_agreement` | "약관 동의 이력 = 탈퇴 처리 완료 시까지" |
| `meeting_travel_burden` | `station_path` 에 출발역이 노출 → 출발지 파생 위치정보 |

### C. 행 유지 + 개인정보 필드 NULL

| 테이블 | NULL 대상 |
|---|---|
| `meeting_participant` | `latitude`, `longitude`, `departure_label`, `departure_place_name`, `departure_address` |

### D. 그대로 유지

| 테이블 | 근거 |
|---|---|
| `group_member` | 방침 "타 이용자의 권리 보호·분쟁 대응에 필요한 경우 최소 보관". 닉네임은 A에 의해 자동 NULL → "알 수 없음" 표시 |
| `date_vote_record` | 투표 집계 정합성 |
| `meeting_place_pick` | 투표 후보 집계 정합성 |
| `meeting_place_vote` | 익명 집계, 개인 귀속 미노출 |

### E. 외부 저장소

| 대상 | 처리 |
|---|---|
| GCS 프로필 이미지 객체 | 삭제. `profiles/` prefix 만 대상. **실패해도 탈퇴 유지**(best-effort) |

### ⚠️ F. 현재 미구현 — 푸시 알림 구현 시 반드시 포함할 것

`device_token` 테이블은 `V5` 로 생성되어 있으나 **Entity·Repository·Service 등 자바 코드가 전무**하여 데이터가 쓰이지 않는다. 따라서 이번 탈퇴 구현에서 파기 대상에서 제외했다.

> **푸시 알림 기능을 구현하는 시점에 `device_token` 파기를 탈퇴 로직에 추가해야 한다.**
> 방침상 "APNs 기기 토큰·FID·FCM 등록 토큰 = 탈퇴 처리 시까지" 파기 대상이다.

---

## 호스트 승계 규칙

탈퇴자가 `HOST` 인 그룹마다:

| 조건 | 처리 |
|---|---|
| 남은 구성원(탈퇴자 제외) 1명 이상 | `joined_at` 이 **가장 이른** 구성원에게 `HOST` 이전 |
| 남은 구성원 0명 | 그룹을 `CLOSED` 처리 |

- 탈퇴자의 `group_member` 행은 삭제하지 않는다 (역할만 `MEMBER` 로 남음)
- 탈퇴자가 `MEMBER` 인 그룹은 대상 아님

---

## 인증 차단 규칙

- `JwtAuthenticationFilter` 에서 토큰 검증 통과 후 회원 상태를 조회한다
- `ACTIVE` 가 아니면 **인증을 부여하지 않는다** → 보호 경로는 401
- 근거: Access Token 만료 1시간. 미차단 시 탈퇴 후 최대 1시간 동안 타인의 모임 정보 조회 가능
- 개별 API마다 검증을 넣는 방식은 누락 위험이 있어 채택하지 않음
- 조회는 `existsActiveById` **경량 프로젝션**(status만)으로 수행하며, 전체 엔티티를 로드하지 않는다
- 필터를 `@Transactional` 로 감싸지 않는다 (커넥션 2개 동시 점유 방지)
- 성능: PK 인덱스 조회 약 0.5~1ms. 기존 API가 요청당 5~6 쿼리를 쓰므로 영향 미미. 캐시는 도입하지 않음(YAGNI)

---

## Apple 연동 해제 (revoke)

- **App Store 심사 필수 요건**
  > "If your app offers Sign in with Apple, you'll need to use the Sign in with Apple REST API to revoke user tokens when deleting an account." — Apple Developer News (Guideline 5.1.1(v))
- 조건: `social_provider = APPLE` **AND** `X-Apple-Authorization-Code` 헤더 존재 **AND** 자격증명 4종 설정됨
- 위 조건 미충족 시 **skip** 하고 탈퇴는 정상 진행 (revoke 실패로 탈퇴가 막히면 5.1.1(v) 본문 위반)
- 카카오 unlink / 네이버 연동 해제는 범위 외

---

## 재가입 규칙

- 동일 소셜 계정으로 재로그인 시 **신규 회원**으로 생성 (`social_user_id` 치환으로 기존 행과 매칭 불가)
- 과거 그룹/모임 복구 없음. 재가입 제한 기간 없음
- `AuthService` 코드 변경 불필요

---

## 트랜잭션 규칙

| 구분 | 범위 |
|---|---|
| **트랜잭션 내** | 호스트 승계 → 모임 참여 데이터 파기 → 개인 소유 데이터 파기 → 회원 익명화 |
| **트랜잭션 외** | GCS 이미지 삭제, Apple revoke (둘 다 실패해도 탈퇴 유지, warn 로그) |

- 파기는 비가역이므로 부분 파기 상태를 허용하지 않는다
- 호스트 승계를 **가장 먼저** 수행한다 (익명화가 선행되면 승계 후보 판정에 혼선)

---

## 로깅 규칙

- 탈퇴 처리 결과를 로그로 남기되 **닉네임·이메일 등 개인 식별정보를 포함하지 않는다**
- `memberId` 수준까지만 기록

---

## 범위 제외

- 관리자용 계정 복구
- 물리 삭제 배치(스케줄러) — 남는 뼈대가 식별 불가하므로 방침상 불요
- 그룹 나가기 / 호스트 위임 API
- 카카오·네이버 연동 해제
- 탈퇴 사유 수집
