# U2 — Logical Components (경량)

- 신규 로지컬 컴포넌트 없음 (큐/캐시/서킷브레이커 불필요 규모)
- 기존 컴포넌트 재사용: PostgreSQL+PostGIS(Cloud SQL), Spring `@Transactional`, 기존 ErrorCode/BusinessException 패턴
