# Application Design — 회원 탈퇴 기능

- 작성일: 2026-08-24
- 입력: `requirements-member-withdrawal.md` (R1~R10), `application-design-plan-member-withdrawal.md` (D1=C, D2=B, D3=A)

---

## 0-0. 구현 착수 조건 — **선행 조건 없음**

**Apple 자격증명이나 iOS 앱 작업 없이 지금 바로 전체 구현이 가능하다.**

| 항목 | 착수 필요 | 미준비 시 동작 |
|---|---|---|
| Apple 자격증명 4종 | **불필요** | `AppleTokenRevokerImpl` 이 no-op으로 동작(§5-2). 탈퇴 전 기능 정상 |
| iOS `X-Apple-Authorization-Code` | **불필요** | 헤더 null → revoke skip. 탈퇴 정상 진행 |
| Flyway 마이그레이션 | **불필요** | 스키마 변경 없음 |

- 자격증명은 **App Store 심사 제출 전까지만** 준비하면 되며, 준비되면 환경변수 주입만으로 활성화된다. **코드 재작업 없음.**
- 따라서 `AppleTokenRevoker` 는 이번 구현에 **포함**한다(스켈레톤이 아니라 완성 구현). 단 설정이 없으면 스스로 skip한다.

---

## 0. 설계 중 발견된 이슈 (구현 전 확인 필요)

### 이슈 1. `device_token` — 자바 코드가 존재하지 않음

`V5__create_device_token.sql` 로 테이블은 생성되어 있으나, **Entity·Repository·Service 어떤 자바 코드도 없다.** 즉 현재 이 테이블에 쓰기가 발생하지 않으며 데이터가 비어 있다.

**결정**: 이번 스코프에서 `device_token` 파기 로직을 구현하지 않는다. 대신 **푸시 알림 기능 구현 시 파기 대상에 반드시 포함**하도록 `fc14/rules.md` 에 명시한다.
(빈 테이블을 위해 Entity/Repository를 신규 생성하는 것은 과설계)

### 이슈 2. `TermsAgreementRepository` 의 기존 정책과 충돌

```java
/** DELETE 금지. INSERT만 허용. */
public interface TermsAgreementRepository { ... }
```

기존 인터페이스에 **DELETE 금지** 주석이 명시되어 있으나, 개인정보 처리방침("약관 동의 이력 = 탈퇴 처리 완료 시까지")상 파기가 필요하다.

**결정**: 방침이 우선한다. `deleteAllByMemberId` 를 추가하고 주석을 아래로 갱신한다.

```java
/** 일반 DELETE 금지(INSERT only). 단, 회원 탈퇴 시 파기는 예외. */
```

---

## 1. 컴포넌트 정의

### 1-1. 신규 컴포넌트

| 컴포넌트 | 위치 | 책임 |
|---|---|---|
| `MemberWithdrawalService` | `member/application/` | 탈퇴 유스케이스 오케스트레이션. 파기 순서·트랜잭션 경계 관리 (D2=B) |
| `AppleTokenRevoker` | `auth/application/` (인터페이스) | Apple 연동 해제 추상화 |
| `AppleTokenRevokerImpl` | `auth/infrastructure/social/` | client_secret(ES256 JWT) 생성 → token 교환 → revoke 호출. 설정 미주입 시 no-op |
| `AppleRevokeProperties` | `auth/infrastructure/social/` | Team ID / Key ID / `.p8` / Client ID 바인딩 (`@ConfigurationProperties`) |

### 1-2. 변경 컴포넌트

| 컴포넌트 | 변경 내용 |
|---|---|
| `Member` (도메인) | `withdraw()` 메서드 추가 — 상태 전이 + 개인정보 필드 익명화 |
| `GroupMember` (도메인) | `promoteToHost()` 메서드 추가 |
| `MeetingParticipant` (도메인) | `clearDeparture()` 메서드 추가 — 좌표·출발지 메타 NULL |
| `MemberController` | `DELETE /api/v1/members/me` 핸들러 추가 |
| `JwtAuthenticationFilter` | 토큰 검증 후 회원 상태 확인 (R9) |
| `PlaceVoteService` | `getVoteParticipants` 에 `isActive()` 가드 추가 (R8) |
| `StorageService` / `GcsStorageClient` | `delete(objectKey)` 신규 — 현재 삭제 기능 없음 |
| `ErrorCode` | `MEMBER_ALREADY_WITHDRAWN` 추가 (`MEMBER_007`) |
| 각 Repository | 파기용 메서드 추가 (§3) |

---

## 2. 컴포넌트 메서드 시그니처

### 2-1. 도메인

```java
// auth/domain/Member.java
/** 탈퇴 처리 — 상태 전이 + 개인정보 익명화 (뼈대만 유지) */
public void withdraw(String anonymizedSocialUserId)
// status=WITHDRAWN, deletedAt=now, nickname/email/profileImageUrl=null,
// socialUserId=anonymizedSocialUserId

/** 탈퇴 여부 */
public boolean isWithdrawn()
```

```java
// group/domain/GroupMember.java
/** 호스트 승계 — 역할을 HOST로 전환 */
public void promoteToHost()
```

```java
// meeting/domain/MeetingParticipant.java
/** 출발지 정보 파기 — 좌표 및 출발지 메타를 모두 제거 (참여 이력은 유지) */
public void clearDeparture()
// latitude/longitude/departureLabel/departurePlaceName/departureAddress = null
```

### 2-2. 애플리케이션 서비스

```java
// member/application/MemberWithdrawalService.java
/**
 * 회원 탈퇴.
 * @param memberId 탈퇴 대상 (인증 principal)
 * @param appleAuthorizationCode Apple 연동 해제용 코드 (nullable)
 */
public void withdraw(Long memberId, String appleAuthorizationCode)
```

```java
// auth/application/AppleTokenRevoker.java
/** Apple 연동 해제. 실패·미설정 시 예외를 던지지 않고 false 반환 (best-effort) */
boolean revoke(String authorizationCode);
```

```java
// storage/application/StorageService.java
/** 객체 삭제. 실패 시 예외 대신 false 반환 */
public boolean delete(String objectKey)
```

### 2-3. Presentation

```java
// member/presentation/MemberController.java
@Operation(summary = "회원 탈퇴")
@DeleteMapping("/me")
public ResponseEntity<Void> withdraw(
        Authentication auth,
        @RequestHeader(value = "X-Apple-Authorization-Code", required = false) String appleCode)
// 204 No Content
```

**D1=C 확정** — `appleAuthorizationCode` 는 커스텀 헤더 `X-Apple-Authorization-Code` 로 전달한다. 액세스 로그에 남지 않고, DELETE 바디 누락 리스크도 없다.

---

## 3. 리포지토리 메서드 추가 (파기 매트릭스 → 코드 매핑)

| 리포지토리 | 추가 메서드 | 대응 요구사항 |
|---|---|---|
| `RefreshTokenRepository` | `void deleteAllByMemberId(Long memberId)` | R3 — 기존 `revokeAllByMemberId` 는 폐기만 하므로 "파기"에 부족 |
| `DeparturePlaceRepository` | `void deleteAllByMemberId(Long memberId)` | R3 |
| `TermsAgreementRepository` | `void deleteAllByMemberId(Long memberId)` | R3 (§0 이슈 2) |
| `MeetingTravelBurdenRepository` | `void deleteAllByMemberId(Long memberId)` | R4 |
| `MeetingParticipantRepository` | `List<MeetingParticipant> findByMemberId(Long memberId)`<br>`void saveAll(List<MeetingParticipant>)` (기존 존재) | R4 — 출발지 필드 NULL 처리 대상 조회 |
| `GroupMemberRepository` | (추가 없음) `findByMemberId`·`findByGroupId`·`save` 기존 활용 | R6 |
| `GroupRepository` | (추가 없음) `findById`·`save` 기존 활용, `Group.close()` 기존 존재 | R6 |
| `MemberRepository` | `boolean existsActiveById(Long memberId)` — 인증 필터 전용 경량 조회 | R2, R9 |

---

## 4. 서비스 오케스트레이션

### 4-1. 처리 순서

```
withdraw(memberId, appleCode)

  [사전 검증]
  1. 회원 조회 → 없으면 MEMBER_NOT_FOUND
  2. 이미 WITHDRAWN → MEMBER_ALREADY_WITHDRAWN
  3. 프로필 이미지 objectKey 확보 (트랜잭션 후 삭제용)

  [트랜잭션 시작] ─────────────────────────────
  4. 호스트 승계 (R6)
       for each group where role=HOST:
         남은 구성원 있음 → joinedAt 최소 구성원.promoteToHost() + save
         남은 구성원 없음 → group.close() + save
  5. 모임 참여 데이터 파기 (R4)
       participants = findByMemberId(memberId)
       each.clearDeparture() → saveAll
       travelBurdenRepository.deleteAllByMemberId(memberId)
  6. 개인 소유 데이터 파기 (R3)
       departurePlaceRepository.deleteAllByMemberId(memberId)
       termsAgreementRepository.deleteAllByMemberId(memberId)
       refreshTokenRepository.deleteAllByMemberId(memberId)
  7. 회원 익명화 (R2)
       member.withdraw("withdrawn_" + UUID.randomUUID())
       memberRepository.save(member)
  [트랜잭션 종료] ─────────────────────────────

  [트랜잭션 외부 — best-effort, 실패해도 탈퇴 유지]
  8. 프로필 이미지 객체 삭제 (R5) — 실패 시 warn 로그
  9. Apple revoke (R7) — provider=APPLE && appleCode != null 일 때만. 실패 시 warn 로그
```

### 4-2. 트랜잭션 경계 설계 근거

- **4~7은 단일 트랜잭션**: 파기는 비가역이므로 일부만 지워지는 상태를 허용하지 않는다.
- **8~9는 트랜잭션 외부**: 외부 시스템(GCS·Apple) 호출을 트랜잭션에 넣으면 네트워크 지연이 DB 커넥션을 점유하고, 외부 장애가 탈퇴 자체를 막는다. R7 요구사항("실패해도 탈퇴는 진행")과도 일치한다.
- **순서 근거**: 호스트 승계(4)를 가장 먼저 수행한다. 회원 익명화(7)가 먼저 일어나면 승계 로직이 탈퇴자를 후보로 오인할 여지가 있다.

### 4-3. 승계 대상 선정 규칙 (R6 구체화)

```
candidates = groupMemberRepository.findByGroupId(groupId)
               .filter(gm -> !gm.getMemberId().equals(withdrawingMemberId))
               .sorted(by joinedAt ASC)

candidates.isEmpty() → group.close()
else                 → candidates.get(0).promoteToHost()
```

- 탈퇴자의 `group_member` 행은 **삭제하지 않는다** (Q4=A). 역할만 `MEMBER` 로 남는다.
- 이미 다른 HOST가 존재하는 그룹은 승계 대상이 아니다 (탈퇴자가 HOST인 그룹만 순회).

---

## 5. Apple Revoke 설계 (R7)

### 5-1. 동작

```
revoke(authorizationCode)
  1. 설정 4종 미주입 → false 반환 (no-op, INFO 로그)     ← 로컬/테스트 환경 보호
  2. client_secret 생성
       header  : alg=ES256, kid=<Key ID>
       payload : iss=<Team ID>, sub=<Client ID>,
                 aud=https://appleid.apple.com, iat=now, exp=now+5m
       sign    : .p8 개인키 (EC)
  3. POST https://appleid.apple.com/auth/token
       grant_type=authorization_code, code, client_id, client_secret
       → refresh_token 획득
  4. POST https://appleid.apple.com/auth/revoke
       token=<refresh_token>, token_type_hint=refresh_token,
       client_id, client_secret
  5. 2xx → true / 그 외·예외 → false (warn 로그, 예외 전파 금지)
```

### 5-2. 설정 (환경변수 주입)

```yaml
apple:
  revoke:
    team-id:     ${APPLE_TEAM_ID:}
    key-id:      ${APPLE_KEY_ID:}
    client-id:   ${APPLE_CLIENT_ID:}
    private-key: ${APPLE_PRIVATE_KEY:}   # .p8 내용 (PEM)
```

- 기본값을 빈 문자열로 두어 **미설정 시 자동 skip**. 개발 환경에서 탈퇴 기능이 막히지 않는다.
- 값은 배포 환경 시크릿으로만 주입한다. 코드·문서·명령어에 평문으로 기록하지 않는다.

---

## 6. 컴포넌트 의존 관계

### 6-1. 의존 매트릭스

| 호출자 | 피호출자 | 목적 |
|---|---|---|
| `MemberController` | `MemberWithdrawalService` | 탈퇴 요청 위임 |
| `MemberWithdrawalService` | `MemberRepository` | 조회·익명화 저장 |
| `MemberWithdrawalService` | `RefreshTokenRepository` | 토큰 파기 |
| `MemberWithdrawalService` | `DeparturePlaceRepository` | 출발지 파기 |
| `MemberWithdrawalService` | `TermsAgreementRepository` | 동의 이력 파기 |
| `MemberWithdrawalService` | `GroupMemberRepository`, `GroupRepository` | 호스트 승계 |
| `MemberWithdrawalService` | `MeetingParticipantRepository`, `MeetingTravelBurdenRepository` | 참여 데이터 파기 |
| `MemberWithdrawalService` | `StorageService` | 프로필 이미지 삭제 |
| `MemberWithdrawalService` | `AppleTokenRevoker` | 연동 해제 |
| `JwtAuthenticationFilter` | `MemberRepository` | 상태 검증 (R9) |

**D3=A 적용** — 리포지토리 직접 주입 방식. 총 9개 의존성이 한 서비스에 모이지만, 파기 대상이 한 파일에서 전부 보이는 것이 개인정보 파기 누락 방지에 유리하다는 판단이다.

### 6-2. 데이터 흐름

```
+-------------------+
|  iOS App          |
|  (탈퇴 + Apple    |
|   재인증 code)    |
+---------+---------+
          | DELETE /api/v1/members/me
          | X-Apple-Authorization-Code: xxx
          v
+-------------------+
| MemberController  |
+---------+---------+
          v
+---------------------------+
| MemberWithdrawalService   |
+----+-----+-----+-----+----+
     |     |     |     |
     |     |     |     +--> [TX 외부] AppleTokenRevoker --> appleid.apple.com
     |     |     +--------> [TX 외부] StorageService     --> GCS
     |     |
     |     +--------------> [TX] group   : GroupMember / Group
     |
     +--------------------> [TX] auth    : Member / RefreshToken
                            [TX] member  : DeparturePlace / TermsAgreement
                            [TX] meeting : MeetingParticipant / TravelBurden
```

---

## 7. R9 인증 필터 설계

```java
// JwtAuthenticationFilter.doFilterInternal
if (token != null && jwtProvider.validateToken(token)) {
    Long memberId = jwtProvider.getMemberId(token);
    if (memberRepository.existsActiveById(memberId)) {
        SecurityContextHolder.getContext().setAuthentication(...);
    }
    // 비활성이면 인증 미부여 → EntryPoint가 401 처리
}
filterChain.doFilter(request, response);
```

```java
// auth/domain/MemberRepository.java (추가)
/** 활성 회원 존재 여부. 인증 필터 전용 경량 조회 */
boolean existsActiveById(Long memberId);
```

```java
// MemberJpaRepository — status만 조회 (전체 엔티티 로드 회피)
@Query("select count(m) > 0 from MemberJpaEntity m where m.id = :id and m.status = 'ACTIVE'")
boolean existsActiveById(@Param("id") Long id);
```

- 인증을 부여하지 않고 체인을 계속 진행시킨다. 보호 경로면 `JwtAuthenticationEntryPoint` 가 401을 반환하고, 공개 경로는 정상 동작한다.
- **`findById` 로 전체 엔티티를 로드하지 않는다.** 필요한 값은 `status` 하나이므로 count 프로젝션으로 처리한다.

### 성능 검토 결과

| 항목 | 평가 |
|---|---|
| 쿼리 비용 | PK 인덱스 조회, Cloud SQL 동일 리전 왕복 약 0.5~1ms |
| 상대 비용 | 기존 API는 요청당 이미 5~6 쿼리 실행(예: `MeetingDetailService`). +1은 미미 |
| 커넥션 풀 | 필터 쿼리는 **트랜잭션 밖**이라 즉시 반납 → 서비스 트랜잭션과 **동시 점유하지 않음**. Hikari 기본 10개로 충분 |
| 주의 | 필터를 `@Transactional` 로 감싸면 요청당 커넥션 2개를 점유하게 되므로 **감싸지 말 것** |
| 미인증 요청 | 토큰이 없으면 조회 자체가 발생하지 않음 (로그인·Swagger 등 공개 경로) |

**확장 여지 (지금은 적용하지 않음)**: 트래픽 증가 시 Caffeine 로컬 캐시(TTL 1분) + 탈퇴 시 evict 로 조회를 사실상 제거할 수 있다. 현재 트래픽 규모에서는 캐시 무효화 복잡도만 늘어나므로 도입하지 않는다 (YAGNI).

---

## 8. 영향 없음 확인 (재확인)

아래는 **수정하지 않는다.** 근거는 `requirements-member-withdrawal.md` §2-2.

| 대상 | 사유 |
|---|---|
| 중간지점 산출 SQL | `ST_Collect` 가 NULL 좌표를 자동 제외 |
| `PlaceVoteService` 이동부담 계산 | `hasCoordinate()` 필터 기존재 |
| `getPlaceTravelBurden` | 스냅샷 없는 멤버 null 처리 기존재 |
| `AuthService` | `social_user_id` 치환으로 재가입이 자동으로 신규 생성 경로를 탄다 (R10) |
| Flyway | 스키마 변경 없음 |

---

## 9. 설계 일관성 검증

- [x] R1~R10 전 항목이 컴포넌트·메서드로 매핑됨 (단, R3의 `device_token` 은 §0 이슈 1로 제외)
- [x] 파기 매트릭스 10개 테이블이 §3·§4에 모두 반영됨
- [x] 트랜잭션 경계와 외부 호출 분리 명시
- [x] 미설정 환경에서 기능이 막히지 않음 (Apple skip)
- [x] 신규 마이그레이션 없음
- [x] DDD 레이어 의존 방향 준수 (`presentation → application → domain ← infrastructure`)
