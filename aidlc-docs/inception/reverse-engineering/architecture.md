# System Architecture

## System Overview

Spring Boot 3.4.4 / Java 17 기반 DDD 모놀리스. iOS 클라이언트에 REST API를 제공.
JWT 기반 인증, PostgreSQL + PostGIS 데이터 저장, Flyway 마이그레이션.

## Architecture: Layered DDD (Bounded Context 단위)

```
Presentation  →  Application  →  Domain  ←  Infrastructure
(Controller)     (Service)      (Model,       (JpaEntity,
(DTO)            (UseCase)       Repository    JpaRepository,
                  Txn 경계)       Interface)    RepositoryImpl,
                                               External Client)
```

## Bounded Contexts

```
com.bangawo
├── global/         공통 설정·보안·예외처리
├── auth/           소셜 로그인·JWT 인증
├── member/         회원 프로필·출발지·약관
├── group/          [MVP1 신규] 그룹 생성·초대·멤버십
└── meeting/        [MVP1 신규] 모임·날짜투표·생명주기
```

## Data Flow (소셜 로그인 예시)

```
iOS App
  → POST /api/v1/auth/login (providerToken)
  → JwtAuthenticationFilter (공개 엔드포인트 통과)
  → AuthController
  → AuthService.socialLogin()
      → KakaoAuthClient.getUserInfo(token)     [외부 소셜 API]
      → MemberRepository.findBy...()            [domain interface]
          → MemberRepositoryImpl                [infra 구현체]
              → MemberJpaRepository             [Spring Data JPA]
                  → PostgreSQL
      → JwtProvider.generateAccessToken()
  → LoginResponse (accessToken, refreshToken)
```

## Integration Points

| 종류 | 대상 | 목적 |
|---|---|---|
| 외부 API | 카카오 OAuth | 소셜 로그인 토큰 검증 |
| 외부 API | 네이버 OAuth | 소셜 로그인 토큰 검증 |
| 외부 API | 애플 Sign In | 소셜 로그인 토큰 검증 |
| DB | PostgreSQL + PostGIS | 데이터 저장, 좌표 타입 |
| 마이그레이션 | Flyway | 스키마 버전 관리 |
