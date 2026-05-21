# Code Quality Assessment

## 테스트 커버리지
- **전체**: 낮음 (2개 테스트 클래스만 확인: JwtProviderTest, GlobalExceptionHandlerTest)
- **단위 테스트**: 일부 (global 레이어 중심)
- **통합 테스트**: 미확인

## 코드 품질 지표
- **Linting**: Lombok + Spring 규약으로 일관성 유지
- **코드 스타일**: 일관됨 — DDD 레이어 분리 철저히 준수
- **문서화**: Swagger 애노테이션 + 핵심 주석 있음

## 잘 적용된 패턴
- **도메인·JPA 분리**: `*JpaEntity` / `*RepositoryImpl` 패턴 철저
- **비즈니스 예외**: `BusinessException(ErrorCode)` 통일
- **생성자 주입**: `@RequiredArgsConstructor` 일관 사용
- **정적 팩토리**: `Member.create(...)` 패턴
- **보안**: Refresh Token SHA-256 해시 저장 (원문 저장 안 함)

## 주의 사항
- `auth` 컨텍스트에 `Member` 도메인이 위치 — MVP1 이후 `member` 컨텍스트로의 이동 고려 필요
- 출발지 최대 개수 기획(3개)과 코드(10개) 불일치 — MVP1에서 기획 기준(3개) 적용
