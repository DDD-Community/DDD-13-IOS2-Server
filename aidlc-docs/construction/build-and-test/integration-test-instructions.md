# Integration Test Instructions

## Status
- **현재 구현 없음** — 통합 테스트는 E2E 검증 단계(운영 환경)에서 수행
- 단위 테스트로 애플리케이션 로직 100% 커버, Spring Context 기동 테스트는 Cloud Run 배포 후 수동 확인

## 수동 통합 검증 절차

### 환경
- URL: `https://bangawo-server-gzfcbbuf4q-du.a.run.app`
- Auth: JWT Bearer Token (카카오/네이버/애플 OAuth 로그인 발급)

### 핵심 플로우 체크리스트 (기존)
- [ ] `POST /api/v1/meetings/{id}/place-vote` (투표 세션 생성, VOTING 전환)
- [ ] `POST /api/v1/meetings/{id}/place-vote/submit` (투표 제출)
- [ ] `GET /api/v1/meetings/{id}/place-vote` (투표 현황 — 이동부담 포함)
- [ ] `GET /api/v1/meetings/{id}/place-result` (확정 결과 — CONFIRMED 후)
- [ ] 스케줄러 자동 VOTING 전환 (담기 마감 → 3일 투표 세션 자동 생성)
- [ ] 스케줄러 자동 확정 (투표 마감 → 4단계 순위 → CONFIRMED)

### 회원 탈퇴 (FC-14) — 신규 체크리스트
- [ ] `DELETE /api/v1/members/me` — 정상 요청 시 204, 이후 `GET /api/v1/members/me` 재호출 시 **같은 Access Token으로 401** (R9, 만료 대기 없이 즉시 차단)
- [ ] 탈퇴 후 DB 확인
  - `member`: status=WITHDRAWN, nickname/email/profile_image_url=NULL, social_user_id가 `withdrawn_...`로 치환
  - `refresh_token` / `departure_place` / `terms_agreement` / `meeting_travel_burden`: 해당 회원 행 0건
  - `meeting_participant`: 행은 유지, latitude/longitude/departure_* 컬럼 NULL
  - `group_member`: 행 유지 (역할만 확인)
- [ ] 탈퇴자가 그룹 호스트였던 경우
  - 잔여 구성원 있음 → `joined_at` 최소 구성원의 `group_member.role`이 HOST로 변경됐는지
  - 잔여 구성원 없음 → `group_info.status`가 CLOSED로 변경됐는지
- [ ] 프로필 이미지가 있던 회원 탈퇴 시 GCS 버킷에서 `profiles/{memberId}/...` 객체가 삭제됐는지 (콘솔 확인)
- [ ] Apple 로그인 회원 + `X-Apple-Authorization-Code` 헤더 전달 시 revoke 호출 로그 확인 (자격증명 미설정 환경에서는 skip 로그만 확인)
- [ ] 동일 소셜 계정으로 재로그인 → 신규 회원(`firstSocialLogin=true`)으로 가입 플로우 진입하는지
- [ ] `GET /api/v1/meetings/{id}/place-vote/participants` — 탈퇴 회원의 `name`/`profileImageUrl`이 `null`로 노출되는지

## Flyway 마이그레이션 검증
```sql
-- 적용 확인
SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;
```
> 이번 사이클(FC-14)은 **신규 마이그레이션 없음** — 기존 `member.status`/`deleted_at`(V2), `meeting_participant` nullable 컬럼(V15/V30)만 재사용.
