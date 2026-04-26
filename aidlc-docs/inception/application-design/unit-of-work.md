# Unit of Work

> **배포 모델**: 모놀리스 단일 서비스  
> **유닛 = 모듈**: 패키지 레벨 분리, 하나의 Spring Boot 앱으로 배포

---

## Unit 1: Global (공통 인프라)

**패키지**: `com.bangawo.global`

| 항목 | 내용 |
|---|---|
| 목적 | 모든 API의 기반 인프라 |
| 포함 | JwtProvider, JwtAuthenticationFilter, SecurityConfig, GlobalExceptionHandler, ErrorResponse, Coordinate 값객체 |
| DB 테이블 | 없음 |
| 외부 의존 | 없음 |
| 선행 조건 | 없음 (첫 번째 구현) |

**산출물**:
- Spring Security 설정 (공개/인증 경로, CORS, 세션 Stateless)
- JWT 생성/검증 유틸 (jjwt 0.12.x)
- JWT 인증 필터
- 글로벌 예외 핸들러 + 커스텀 에러 응답
- Coordinate 값객체

---

## Unit 2: Auth (인증)

**패키지**: `com.bangawo.auth`

| 항목 | 내용 |
|---|---|
| 목적 | 소셜 로그인 + JWT 발급/갱신/폐기 |
| 포함 | AuthService, SocialAuthClient(인터페이스), KakaoAuthClient, NaverAuthClient, AppleAuthClient, RefreshToken 도메인/JPA, AuthController |
| DB 테이블 | `member` (생성), `refresh_token` |
| 외부 의존 | Kakao API, Naver API, Apple JWKS |
| 선행 조건 | Unit 1 (Global) |

**산출물**:
- 소셜 로그인 API (`POST /api/v1/auth/login`)
- 토큰 갱신 API (`POST /api/v1/auth/refresh`)
- 로그아웃 API (`POST /api/v1/auth/logout`)
- Member 도메인 엔티티 + JPA 엔티티 + Mapper
- RefreshToken 도메인 엔티티 + JPA 엔티티 + Mapper
- 소셜 공급자별 인증 클라이언트 (카카오/네이버/애플)
- Flyway 마이그레이션: `member`, `refresh_token` 테이블

**소셜 로그인에 필요한 정보** (사용자 제공 필요):
- 카카오: REST API 키
- 네이버: Client ID + Client Secret
- 애플: Team ID + Key ID + Private Key (.p8 파일) + Client ID (Service ID)

---

## Unit 3: Member (회원)

**패키지**: `com.bangawo.member`

| 항목 | 내용 |
|---|---|
| 목적 | 프로필, 출발지, 약관, 디바이스 토큰 |
| 포함 | MemberService, DeparturePlaceService, TermsService, DeviceTokenService, NicknameValidator, 각 도메인/JPA/Controller |
| DB 테이블 | `departure_place`, `terms`, `terms_agreement`, `device_token` |
| 외부 의존 | 없음 |
| 선행 조건 | Unit 2 (Auth) — member 테이블 존재 필요 |

**산출물**:
- 회원가입 API (`POST /api/v1/members/register`)
- 프로필 조회/수정 API
- 출발지 CRUD API (5개)
- 약관 조회/동의 API (2개)
- 디바이스 토큰 등록/삭제 API (2개)
- 금칙어 필터 (정적 리스트 + 정규식 + 자모 정규화)
- Flyway 마이그레이션: `departure_place`, `terms`, `terms_agreement`, `device_token` 테이블 + 약관 시드 데이터

---

## 코드 조직 전략

```
src/main/java/com/bangawo/
├── global/
│   ├── config/          # SecurityConfig, WebConfig
│   ├── security/        # JwtProvider, JwtAuthenticationFilter
│   ├── error/           # GlobalExceptionHandler, ErrorResponse, BusinessException
│   └── common/          # Coordinate
├── auth/
│   ├── domain/          # RefreshToken, SocialProvider, MemberRepository(인터페이스)
│   ├── application/     # AuthService, SocialAuthClient(인터페이스)
│   ├── infrastructure/  # JPA 엔티티, Repository 구현, 소셜 클라이언트 구현
│   └── presentation/    # AuthController, DTO
└── member/
    ├── domain/          # DeparturePlace, Terms, TermsAgreement, DeviceToken, NicknameValidator
    ├── application/     # MemberService, DeparturePlaceService, TermsService, DeviceTokenService
    ├── infrastructure/  # JPA 엔티티, Repository 구현, Mapper
    └── presentation/    # Controllers, DTO

src/main/resources/
├── application.yml
├── application-local.yml
├── db/migration/        # Flyway 스크립트
└── docker-compose.yml   # PostgreSQL + PostGIS
```
