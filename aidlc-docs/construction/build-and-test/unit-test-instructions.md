# Unit Test Instructions

> 최신 갱신: 2026-08-24 — FC-14 회원 탈퇴 반영

## Test Framework
- **JUnit 5** + **Mockito** (`@Mock` / `@InjectMocks` / `@ExtendWith(MockitoExtension.class)`)
- **AssertJ** for assertions
- 협력자 많은 서비스는 `@MockitoSettings(strictness = LENIENT)` 사용

## Test Scope (118 tests, 0 failures)

### 회원 탈퇴 (FC-14) — 이번 사이클 핵심
| Test Class | Coverage |
|---|---|
| `MemberWithdrawalServiceTest` | 정상 탈퇴(익명화 필드) / 이미 탈퇴 / 회원 없음 / 호스트 승계(잔여 있음→선임자·잔여 없음→CLOSED) / 모임참여 출발지 파기+이력유지 / **Apple revoke 조건 3분기**(provider+code 모두 충족/provider만/code만) / 프로필이미지 없을 때 storage 미호출 |
| `JwtAuthenticationFilterTest` | 활성 회원 → 인증 부여 / 탈퇴 회원(`existsActiveById=false`) → 인증 미부여 / 토큰 없음 → 조회 자체 생략 |
| `AppleTokenRevokerImplTest` | 자격증명 미설정 → 예외 없이 false (no-op) |
| `MemberTest` | `withdraw()` 상태전이·익명화, `isWithdrawn()`, id/socialProvider/createdAt 유지 |
| `GroupMemberTest` | `promoteToHost()` 역할 전환 |
| `MeetingParticipantTest` | `clearDeparture()` 좌표+메타 null, 참여이력 유지 |
| `PlaceVoteServiceTest`(갱신) | `getVoteParticipants` 탈퇴 회원 name/profileImageUrl null 가드 케이스 추가. 기존 `member()` 헬퍼에 status=ACTIVE 기본값 보강(회귀 방지) |

### 기타 (기존 유지 — FC-8~13 등)
- `PlaceConfirmServiceTest`, `MeetingPlacePickTest`, `PlacePickServiceTest`, `MeetingComputeListStatusTest`, `MeetingCreateTest`, `MeetingLocationPhaseTest`, `MeetingPickDeadlineTest`, `MeetingListServiceTest`, `PlaceSelectionServiceTest`, `GroupServiceTest`, `DeparturePlaceServiceTest` 등

## Run Command
```bash
./gradlew test
# 이번 사이클 추가/변경 클래스만
./gradlew test --tests "com.bangawo.member.application.MemberWithdrawalServiceTest"
./gradlew test --tests "com.bangawo.global.security.JwtAuthenticationFilterTest"
./gradlew test --tests "com.bangawo.auth.infrastructure.social.AppleTokenRevokerImplTest"
./gradlew test --tests "com.bangawo.meeting.application.PlaceVoteServiceTest"
```

## Key Patterns
- `@ExtendWith(MockitoExtension.class)` — Spring context 불필요
- `@InjectMocks` 대상에 새 의존성 추가 시 반드시 `@Mock` 선언 추가 (누락 시 NPE)
- **TransactionTemplate 목킹**: `MemberWithdrawalService`는 `TransactionTemplate.executeWithoutResult(Consumer)`를 사용. 테스트에서는 `doAnswer`로 콜백을 즉시 실행시켜 실제 트랜잭션 없이 검증한다.
  ```java
  doAnswer(invocation -> {
      Consumer<Object> callback = invocation.getArgument(0);
      callback.accept(null);
      return null;
  }).when(transactionTemplate).executeWithoutResult(any());
  ```
- 비즈니스 규칙 위반: `assertThatThrownBy(...).extracting(e -> ((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.XXX)`
