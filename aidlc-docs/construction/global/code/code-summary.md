# Unit 1: Global — Code Summary

## 생성된 파일

### 프로젝트 설정
- `build.gradle` — Spring Boot 3.4.4, Java 17, jjwt 0.12.6, Flyway, Testcontainers
- `settings.gradle`
- `gradle/wrapper/` — Gradle 8.12
- `docker-compose.yml` — PostgreSQL 15 + PostGIS 3.4
- `src/main/resources/application.yml` — 공통 설정 (JWT, Flyway)
- `src/main/resources/application-local.yml` — 로컬 DB, Swagger
- `src/main/resources/db/migration/V1__init_postgis.sql`
- `.env` — 환경변수 템플릿
- `.gitignore`

### 소스 코드 (`com.bangawo.global`)
- `BangawoApplication.java` — 메인 클래스
- `global/common/Coordinate.java` — 위도/경도 값객체
- `global/error/ErrorCode.java` — 에러 코드 enum (10개)
- `global/error/ErrorResponse.java` — `{ "code", "message" }` DTO
- `global/error/BusinessException.java` — 비즈니스 예외
- `global/error/GlobalExceptionHandler.java` — 전역 예외 핸들러
- `global/security/JwtProvider.java` — JWT 생성/검증 (jjwt 0.12.x)
- `global/security/JwtAuthenticationFilter.java` — Bearer 토큰 필터
- `global/security/JwtAuthenticationEntryPoint.java` — 인증 실패 응답
- `global/config/SecurityConfig.java` — Spring Security 설정

### 테스트
- `JwtProviderTest.java` — 4개 테스트
- `GlobalExceptionHandlerTest.java` — 2개 테스트

## 빌드 결과
- 컴파일: ✅ 성공
- 테스트: ✅ 6개 전체 통과
