# Components

## 패키지 구조

```
com.bangawo
├── auth/                            # 인증 (소셜 로그인, JWT)
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
├── member/                          # 회원 (프로필, 출발지, 약관, 디바이스 토큰)
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
└── global/                          # 공통 (Security, Error, 값객체)
    ├── config/
    ├── error/
    ├── security/
    └── common/
```

---

## 컴포넌트 정의

### 1. global — 공통

| 컴포넌트 | 책임 |
|---|---|
| `JwtProvider` | JWT 생성/검증/파싱 |
| `JwtAuthenticationFilter` | Bearer 토큰 검증, SecurityContext 설정 |
| `SecurityConfig` | Spring Security 설정 (공개/인증 경로, CORS) |
| `GlobalExceptionHandler` | 전역 예외 → `{ "code", "message" }` 변환 |
| `ErrorResponse` | 에러 응답 DTO |
| `Coordinate` | 위도/경도 불변 값객체 |

### 2. auth — 인증

| 컴포넌트 | 레이어 | 책임 |
|---|---|---|
| `SocialProvider` | domain | enum (KAKAO, NAVER, APPLE) |
| `RefreshToken` | domain | 리프레시 토큰 도메인 엔티티 |
| `RefreshTokenRepository` | domain | 저장소 인터페이스 |
| `AuthService` | application | 소셜 로그인 → JWT 발급 오케스트레이션 |
| `SocialAuthClient` | application | 소셜 인증 클라이언트 인터페이스 |
| `KakaoAuthClient` | infrastructure | 카카오 Access Token 검증 |
| `NaverAuthClient` | infrastructure | 네이버 Access Token 검증 |
| `AppleAuthClient` | infrastructure | 애플 ID Token 검증 |
| `RefreshTokenJpaEntity` | infrastructure | JPA 엔티티 |
| `RefreshTokenJpaRepository` | infrastructure | Spring Data JPA |
| `AuthController` | presentation | 로그인/토큰갱신/로그아웃 API |

### 3. member — 회원

| 컴포넌트 | 레이어 | 책임 |
|---|---|---|
| `Member` | domain | 회원 도메인 엔티티 |
| `DeparturePlace` | domain | 출발지 도메인 엔티티 |
| `Terms` | domain | 약관 도메인 엔티티 |
| `TermsAgreement` | domain | 동의 이력 도메인 엔티티 |
| `DeviceToken` | domain | 디바이스 토큰 도메인 엔티티 |
| `NicknameValidator` | domain | 금칙어 필터 |
| `MemberRepository` | domain | 회원 저장소 인터페이스 |
| `DeparturePlaceRepository` | domain | 출발지 저장소 인터페이스 |
| `TermsRepository` | domain | 약관 저장소 인터페이스 |
| `TermsAgreementRepository` | domain | 동의 이력 저장소 인터페이스 |
| `DeviceTokenRepository` | domain | 디바이스 토큰 저장소 인터페이스 |
| `MemberService` | application | 회원가입/프로필 관리 |
| `DeparturePlaceService` | application | 출발지 CRUD |
| `TermsService` | application | 약관 조회/동의 처리 |
| `DeviceTokenService` | application | 토큰 등록/삭제 |
| `MemberJpaEntity` | infrastructure | JPA 엔티티 |
| `DeparturePlaceJpaEntity` | infrastructure | JPA 엔티티 |
| `TermsJpaEntity` | infrastructure | JPA 엔티티 |
| `TermsAgreementJpaEntity` | infrastructure | JPA 엔티티 |
| `DeviceTokenJpaEntity` | infrastructure | JPA 엔티티 |
| `각 JpaRepository` | infrastructure | Spring Data JPA 구현 |
| `각 Mapper` | infrastructure | Domain ↔ JPA Entity 변환 |
| `MemberController` | presentation | 회원가입/프로필 API |
| `DeparturePlaceController` | presentation | 출발지 API |
| `TermsController` | presentation | 약관 API |
| `DeviceTokenController` | presentation | 디바이스 토큰 API |
