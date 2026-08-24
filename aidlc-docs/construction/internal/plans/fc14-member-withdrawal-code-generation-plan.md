# Code Generation Plan — FC-14 회원 탈퇴

- 작성일: 2026-08-24
- 입력: `application-design-member-withdrawal.md`, `aidlc-docs/construction/review/fc14/{rules,api,erd,flow}.md`
- 워크스페이스 루트: `/Users/ym/dev/DDD/Server` (Brownfield, `src/main/java/com/bangawo/...`)
- 마이그레이션 없음 (스키마 변경 없음)

---

## Unit Context
- **대상 컨텍스트**: auth, member, group, meeting, storage, global
- **신규 엔드포인트**: `DELETE /api/v1/members/me`
- **전역 영향**: `JwtAuthenticationFilter` (모든 인증 요청)
- **의존 순서**: ErrorCode/도메인 → Repository → Storage delete → Apple revoke 클라이언트 → 오케스트레이션 서비스 → Controller → 필터(R9) → 가드(R8) → 테스트

---

## Step 1: ErrorCode 추가
- [x] `global/error/ErrorCode.java` — `MEMBER_ALREADY_WITHDRAWN(BAD_REQUEST, "MEMBER_007", "이미 탈퇴한 회원입니다")` 추가

## Step 2: 도메인 로직 추가
- [x] `auth/domain/Member.java` — `deletedAt` 필드 추가, `withdraw(String anonymizedSocialUserId)`, `isWithdrawn()` 메서드 추가
- [x] `auth/infrastructure/persistence/MemberJpaEntity.java` — `from()`/`toDomain()`에 `deletedAt` 매핑 반영 (기존 컬럼 존재, 매핑 누락 보완)
- [x] `group/domain/GroupMember.java` — `promoteToHost()` 메서드 추가
- [x] `meeting/domain/MeetingParticipant.java` — `clearDeparture()` 메서드 추가 (좌표 2 + 출발지 메타 3 = null)

## Step 3: 도메인 단위 테스트
- [x] `src/test/java/com/bangawo/auth/domain/MemberTest.java` — withdraw() 상태전이/필드익명화, isWithdrawn() 검증 (신규 파일 — auth 도메인 테스트 최초)
- [x] `src/test/java/com/bangawo/group/domain/GroupMemberTest.java` — promoteToHost() 검증 (신규 파일)
- [x] `src/test/java/com/bangawo/meeting/domain/MeetingParticipantTest.java` — clearDeparture() 검증 (신규 파일)

## Step 4: 리포지토리 인터페이스 + 구현체 확장 (파기용 메서드)
- [x] `auth/domain/MemberRepository.java` + `MemberJpaRepository.java`(count 프로젝션 쿼리) + `MemberRepositoryImpl.java` — `existsActiveById(Long)` 추가
- [x] `auth/domain/RefreshTokenRepository.java` + `RefreshTokenJpaRepository.java`(`@Modifying @Query` DELETE) + `RefreshTokenRepositoryImpl.java` — `deleteAllByMemberId(Long)` 추가
- [x] `member/domain/departure/DeparturePlaceRepository.java` + `DeparturePlaceJpaRepository.java`(`@Modifying @Query` DELETE) + `DeparturePlaceRepositoryImpl.java` — `deleteAllByMemberId(Long)` 추가
- [x] `member/domain/terms/TermsAgreementRepository.java`(주석 갱신: "일반 DELETE 금지. 단, 회원 탈퇴 시 파기는 예외") + `TermsAgreementJpaRepository.java` + `TermsAgreementRepositoryImpl.java` — `deleteAllByMemberId(Long)` 추가 (엔티티 클래스 주석도 갱신)
- [x] `meeting/domain/MeetingParticipantRepository.java` + `MeetingParticipantJpaRepository.java` + `MeetingParticipantRepositoryImpl.java` — `findByMemberId(Long)` 추가
- [x] `meeting/domain/MeetingTravelBurdenRepository.java` + `MeetingTravelBurdenJpaRepository.java`(`@Modifying @Query` DELETE) + `MeetingTravelBurdenRepositoryImpl.java` — `deleteAllByMemberId(Long)` 추가

## Step 5: Storage 삭제 기능 추가
- [x] `storage/infrastructure/gcs/GcsStorageClient.java` — `delete(String objectKey)` 추가 (성공 true / 예외 시 false, 예외 전파 금지)
- [x] `storage/application/StorageService.java` — `delete(String objectKey)` 추가 (`profiles/` prefix만 대상, `GcsStorageClient.delete` 위임)

## Step 6: Apple Revoke 클라이언트 신규 생성
- [x] `auth/infrastructure/social/AppleRevokeProperties.java` 신규 — `@Component @ConfigurationProperties(prefix="apple.revoke")`, teamId/keyId/clientId/privateKey + `isConfigured()`
- [x] `auth/application/AppleTokenRevoker.java` 신규 (인터페이스) — `boolean revoke(String authorizationCode)`
- [x] `auth/infrastructure/social/AppleTokenRevokerImpl.java` 신규 — client_secret(ES256 JWT, jjwt) 생성 → `/auth/token` 교환 → `/auth/revoke` 호출(RestClient). 미설정/실패 시 예외 전파 없이 false 반환 + warn/info 로그
- [x] `src/main/resources/application.yml` — `apple.revoke.{team-id,key-id,client-id,private-key}` 환경변수 placeholder 추가 (기본값 빈 문자열)

## Step 7: Apple Revoke 단위 테스트
- [x] `src/test/java/com/bangawo/auth/infrastructure/social/AppleTokenRevokerImplTest.java` 신규 — 자격증명 미설정 시 revoke()가 예외 없이 false 반환하는지 검증 (외부 HTTP 호출 없는 케이스만, 네트워크 통합 테스트 제외)

## Step 8: 회원 탈퇴 오케스트레이션 서비스
- [x] `member/application/MemberWithdrawalService.java` 신규 — `withdraw(Long memberId, String appleAuthorizationCode)`
  - 사전 검증(회원 조회/이미 탈퇴 확인/프로필 objectKey 확보)
  - TX(`TransactionTemplate`, Spring Boot 자동 구성 빈 사용): 호스트 승계 → 모임참여데이터 파기(clearDeparture+saveAll, travelBurden 삭제) → 개인소유데이터 파기(departurePlace/termsAgreement/refreshToken 삭제) → 회원 익명화(`withdrawn_{UUID}`)
  - TX 외부: GCS 이미지 삭제(best-effort, StorageService 내부에서 profiles/ prefix 검증), Apple revoke(provider=APPLE && code!=null 조건, best-effort)
  - 개인 식별정보 없는 로깅(memberId만)

## Step 9: 회원 탈퇴 서비스 단위 테스트
- [x] `src/test/java/com/bangawo/member/application/MemberWithdrawalServiceTest.java` 신규
  - 정상 탈퇴(익명화 필드 검증, 파기 리포지토리 호출 검증)
  - 이미 WITHDRAWN → `MEMBER_ALREADY_WITHDRAWN`
  - 존재하지 않는 회원 → `MEMBER_NOT_FOUND`
  - 호스트인 그룹 — 잔여 구성원 있음 → 승계 / 잔여 구성원 없음 → CLOSED
  - 모임 참여 출발지 파기 + 참여이력 유지 검증
  - Apple revoke — provider=APPLE&&code!=null → 호출 / 그 외 조합 → 호출 안 함
  - 프로필 이미지 없으면 storageService.delete 미호출

## Step 10: API 레이어 — Controller 추가
- [x] `member/presentation/MemberController.java` — `DELETE /me` 핸들러 추가 (`Authentication` principal, `X-Apple-Authorization-Code` 옵셔널 헤더, 204 No Content)

## Step 11: 인증 필터 상태 검증 (R9, 전역 영향)
- [x] `global/security/JwtAuthenticationFilter.java` — `MemberRepository` 주입, 토큰 검증 통과 후 `existsActiveById(memberId)` 확인, `false`면 인증 미부여(체인은 계속 진행 → 보호 경로는 EntryPoint가 401)

## Step 12: 인증 필터 단위 테스트
- [x] `src/test/java/com/bangawo/global/security/JwtAuthenticationFilterTest.java` 신규 — 활성 회원 토큰 → 인증 부여 / 탈퇴 회원 토큰(existsActiveById=false) → 인증 미부여 / 토큰 없음 → 조회 자체 없이 체인 진행 검증 (Mockito, FilterChain mock)

## Step 13: 탈퇴 회원 노출 가드 보완 (R8)
- [x] `meeting/application/PlaceVoteService.java` — `getVoteParticipants` 에서 `Member.isActive()` 가드 추가 (닉네임/프로필 `null` 처리), 기존 4곳과 동일 패턴
- [x] `src/test/java/com/bangawo/meeting/application/PlaceVoteServiceTest.java` — 탈퇴 회원 포함 시 name/profileImageUrl null 검증 케이스 추가 (`member()` 헬퍼에 ACTIVE 상태 기본값 보강 + `withdrawnMember()` 헬퍼 신규)

## Step 14: 전체 빌드 확인
- [x] `./gradlew compileJava compileTestJava` — 컴파일 확인 (전체 테스트 실행은 Build & Test 단계에서 수행). BUILD SUCCESSFUL

## Step 15: Review API 문서 갱신 확인 (Step 13.5, MANDATORY)
- [x] `aidlc-docs/construction/review/fc14/api.md` — 실제 구현과 일치 확인 완료(엔드포인트/헤더/응답코드/에러코드/필터변경/getVoteParticipants 가드 모두 일치). 변경 불필요

---

## Story Traceability

| 요구사항 | 대응 Step |
|---|---|
| R1 탈퇴 API | Step 10 |
| R2 회원 익명화 | Step 2, 8 |
| R3 개인 소유 데이터 물리 삭제 | Step 4, 8 |
| R4 모임 참여 데이터 파기 | Step 2, 4, 8 |
| R5 프로필 이미지 삭제 | Step 5, 8 |
| R6 호스트 승계 | Step 2, 8 |
| R7 Apple revoke | Step 6, 8 |
| R8 투표 참여자 가드 | Step 13 |
| R9 인증 즉시 차단 | Step 11 |
| R10 재가입 | 코드 변경 없음 (기존 AuthService 로직으로 충족) |

이 계획은 Code Generation 실행의 단일 기준 문서입니다. 각 단계 완료 시 체크박스를 즉시 갱신합니다.
