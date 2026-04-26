# Code Generation Plan — Unit 1: Global

## Unit Context
- **유닛**: Global (공통 인프라)
- **패키지**: `com.bangawo.global`
- **의존**: 없음 (첫 번째 유닛)
- **DB 테이블**: 없음
- **산출물**: Spring Boot 프로젝트 초기 설정 + JWT + Security + Error Handling + 값객체

---

## Steps

### Step 1: 프로젝트 초기 설정
- [x] `build.gradle` 생성 (Spring Boot 3.x, Java 17, 의존성 전체)
- [x] `settings.gradle` 생성
- [x] `gradle/wrapper/` 설정 (Gradle Wrapper)
- [x] `src/main/resources/application.yml` 생성 (공통 설정)
- [x] `src/main/resources/application-local.yml` 생성 (로컬 프로필)
- [x] `docker-compose.yml` 생성 (PostgreSQL + PostGIS)
- [x] `src/main/java/com/bangawo/BangawoApplication.java` 메인 클래스

### Step 2: 공용 값객체
- [x] `global/common/Coordinate.java` — 위도/경도 불변 값객체

### Step 3: 에러 처리
- [x] `global/error/ErrorCode.java` — 에러 코드 enum
- [x] `global/error/ErrorResponse.java` — 커스텀 에러 응답 DTO `{ "code", "message" }`
- [x] `global/error/BusinessException.java` — 비즈니스 예외 베이스 클래스
- [x] `global/error/GlobalExceptionHandler.java` — `@RestControllerAdvice` 전역 예외 핸들러

### Step 4: JWT
- [x] `global/security/JwtProvider.java` — JWT 생성/검증/파싱 (jjwt 0.12.x)
- [x] `global/security/JwtAuthenticationFilter.java` — OncePerRequestFilter, Bearer 토큰 검증

### Step 5: Spring Security 설정
- [x] `global/config/SecurityConfig.java` — 필터 체인, 공개/인증 경로, CORS, Stateless
- [x] `global/security/JwtAuthenticationEntryPoint.java` — 인증 실패 시 에러 응답

### Step 6: 테스트
- [x] `JwtProviderTest.java` — JWT 생성/검증 단위 테스트
- [x] `GlobalExceptionHandlerTest.java` — 에러 응답 포맷 테스트

### Step 7: 문서
- [x] `aidlc-docs/construction/global/code/code-summary.md` — 생성된 파일 목록 및 요약
