# Integration Test Instructions

## Status
- **현재 구현 없음** — 통합 테스트는 E2E 검증 단계(운영 환경)에서 수행
- 단위 테스트로 애플리케이션 로직 100% 커버, Spring Context 기동 테스트는 Cloud Run 배포 후 수동 확인

## 수동 통합 검증 절차

### 환경
- URL: `https://bangawo-server-gzfcbbuf4q-du.a.run.app`
- Auth: JWT Bearer Token (카카오 OAuth 로그인 발급)

### 핵심 플로우 체크리스트
- [ ] `POST /api/v1/meetings/{id}/place-vote` (투표 세션 생성, VOTING 전환)
- [ ] `POST /api/v1/meetings/{id}/place-vote/submit` (투표 제출)
- [ ] `GET /api/v1/meetings/{id}/place-vote` (투표 현황 — 이동부담 포함)
- [ ] `GET /api/v1/meetings/{id}/place-result` (확정 결과 — CONFIRMED 후)
- [ ] 스케줄러 자동 VOTING 전환 (담기 마감 → 3일 투표 세션 자동 생성)
- [ ] 스케줄러 자동 확정 (투표 마감 → 4단계 순위 → CONFIRMED)

## Flyway 마이그레이션 검증
```sql
-- 적용 확인
SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;
-- V22~V25 포함 여부 확인
```
