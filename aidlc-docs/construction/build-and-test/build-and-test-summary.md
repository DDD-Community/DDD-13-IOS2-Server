# Build and Test Summary

> 최신 갱신: 2026-08-24 — FC-14 회원 탈퇴 사이클

## Build Result
| 항목 | 결과 |
|---|---|
| **Build Tool** | Gradle 8.12 (Wrapper) |
| **Command** | `./gradlew clean build --no-daemon` |
| **Status** | BUILD SUCCESSFUL |
| **Artifacts** | `build/libs/bangawo-0.0.1-SNAPSHOT.jar`, `*-plain.jar` |
| **실행일** | 2026-08-24 |

## Test Result
| 항목 | 결과 |
|---|---|
| **총 테스트 수** | 118 |
| **성공** | 118 |
| **실패** | 0 |
| **에러** | 0 |
| **스킵** | 0 |

> 이전 사이클 96 → 이번 사이클 +22 (회원 탈퇴 신규 7클래스 35건, 그중 기존 `PlaceVoteServiceTest` 갱신분 제외 순증 22건)

## 이번 사이클 변경 (FC-14 회원 탈퇴)

### 신규 컴포넌트
- `MemberWithdrawalService` — 탈퇴 오케스트레이션. `TransactionTemplate`으로 "DB 파기(원자적)"와 "외부 호출(best-effort)"의 트랜잭션 경계를 명시적으로 분리
- `AppleTokenRevoker` / `AppleTokenRevokerImpl` / `AppleRevokeProperties` — Sign in with Apple 계정 삭제 시 토큰 revoke (App Store Guideline 5.1.1(v) 의무). ES256 client_secret(jjwt) → `/auth/token` → `/auth/revoke`. 자격증명 미설정 시 자동 no-op

### 도메인 로직 확장
- `Member.withdraw()` / `isWithdrawn()` — 상태전이 + 5개 필드 익명화(social_user_id는 `withdrawn_{UUID}` 치환)
- `GroupMember.promoteToHost()` — 호스트 승계
- `MeetingParticipant.clearDeparture()` — 좌표·출발지 메타 파기(참여 이력은 유지)

### 파기 인프라 (파기 매트릭스 → 코드)
| 처리 | 테이블 | 방식 |
|---|---|---|
| 물리 삭제 | refresh_token, departure_place, terms_agreement, meeting_travel_burden | `deleteAllByMemberId` (`@Modifying @Query` DELETE, 기존 프로젝트 컨벤션 통일) |
| 필드 NULL, 행 유지 | meeting_participant | `clearDeparture()` + `saveAll` |
| 유지 | group_member, date_vote_record, meeting_place_pick, meeting_place_vote | 변경 없음 (집계·이력 보존) |
| 제외(범위 외) | device_token | 자바 코드 자체가 없어 제외. **푸시 알림 구현 시 반드시 추가** (rules.md에 경고 기재) |

### 전역/횡단 관심사
- `JwtAuthenticationFilter` — `MemberRepository.existsActiveById()` 경량 조회(count 프로젝션, 트랜잭션 밖) 추가. 탈퇴 회원은 Access Token 만료 전이어도 즉시 401 (R9)
- `PlaceVoteService.getVoteParticipants` — 탈퇴 회원 닉네임/프로필 `null` 처리 (기존 4곳과 동일 가드 패턴, R8)
- `StorageService` / `GcsStorageClient` — `delete(objectKey)` 신규 (기존엔 signed URL 발급만 있고 삭제 기능이 없었음)

### API
- `DELETE /api/v1/members/me` — 인증 필수, 본인만, `X-Apple-Authorization-Code` 옵션 헤더, 204 No Content
- `MEMBER_007`(이미 탈퇴한 회원) 에러코드 신규

## Flyway 마이그레이션 (이번 추가)
**없음.** 기존 `member.status`/`deleted_at`(V2), `meeting_participant` nullable 좌표(V15)·출발지 메타(V30) 컬럼을 그대로 재사용.

## Overall Status
- **Build**: Success
- **All Tests**: Pass (118/118)
- **Ready for Operations**: Yes

## 운영 전환 전 확인 필요 (코드 변경 불필요)
- Apple 자격증명 4종(`APPLE_TEAM_ID`/`APPLE_KEY_ID`/`APPLE_CLIENT_ID`/`APPLE_PRIVATE_KEY`)은 App Store 심사 제출 전까지 배포 환경 시크릿으로 주입 필요 (미설정 상태로도 배포·운영 가능, revoke만 skip)
- iOS 클라이언트가 탈퇴 시 Apple 재인증 → `X-Apple-Authorization-Code` 헤더 전달하도록 구현 필요 (서버 단독으로는 revoke 불가)
