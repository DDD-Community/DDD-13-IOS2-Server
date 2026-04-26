# Component Methods

---

## 1. Global

### JwtProvider

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `generateAccessToken(memberId)` | `Long` | `String` | Access JWT 생성 (1시간) |
| `generateRefreshToken(memberId)` | `Long` | `String` | Refresh JWT 생성 (30일) |
| `validateToken(token)` | `String` | `boolean` | 토큰 유효성 검증 |
| `getMemberId(token)` | `String` | `Long` | 토큰에서 회원 ID 추출 |

### GlobalExceptionHandler

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `handleBusinessException(e)` | `BusinessException` | `ErrorResponse` | 비즈니스 예외 처리 |
| `handleValidationException(e)` | `MethodArgumentNotValidException` | `ErrorResponse` | 입력 검증 실패 |
| `handleException(e)` | `Exception` | `ErrorResponse` | 미처리 예외 → 500 |

---

## 2. Auth

### AuthService

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `socialLogin(provider, providerToken)` | `SocialProvider`, `String` | `LoginResponse` | 소셜 로그인 → JWT 발급 |
| `refreshToken(refreshToken)` | `String` | `TokenResponse` | 토큰 갱신 |
| `logout(memberId)` | `Long` | `void` | Refresh Token 폐기 |

### SocialAuthClient (인터페이스)

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `getUserInfo(providerToken)` | `String` | `SocialUserInfo` | 공급자 토큰으로 사용자 정보 조회 |
| `supports(provider)` | `SocialProvider` | `boolean` | 해당 공급자 지원 여부 |

---

## 3. Member

### MemberService

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `register(memberId, request)` | `Long`, `RegisterRequest` | `MemberResponse` | 회원가입 (닉네임 + 출발지 + 약관) |
| `getProfile(memberId)` | `Long` | `MemberResponse` | 프로필 조회 |
| `updateNickname(memberId, nickname)` | `Long`, `String` | `void` | 닉네임 변경 |

### DeparturePlaceService

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `create(memberId, request)` | `Long`, `CreateDeparturePlaceRequest` | `DeparturePlaceResponse` | 출발지 추가 |
| `getAll(memberId)` | `Long` | `List<DeparturePlaceResponse>` | 전체 조회 |
| `update(memberId, placeId, request)` | `Long`, `Long`, `UpdateDeparturePlaceRequest` | `DeparturePlaceResponse` | 수정 |
| `delete(memberId, placeId)` | `Long`, `Long` | `void` | 삭제 |
| `setDefault(memberId, placeId)` | `Long`, `Long` | `void` | 기본 출발지 변경 |

### TermsService

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `getCurrentTerms()` | - | `List<TermsResponse>` | 현재 약관 목록 |
| `agreeTerms(memberId, termsIds)` | `Long`, `List<Long>` | `void` | 약관 동의 |
| `getUnagreedRequiredTerms(memberId)` | `Long` | `List<TermsResponse>` | 미동의 필수 약관 |
| `hasAgreedAllRequired(memberId)` | `Long` | `boolean` | 필수 약관 전체 동의 여부 |

### DeviceTokenService

| 메서드 | 입력 | 출력 | 목적 |
|---|---|---|---|
| `register(memberId, request)` | `Long`, `RegisterDeviceTokenRequest` | `void` | 토큰 등록/갱신 |
| `delete(memberId, token)` | `Long`, `String` | `void` | 토큰 삭제 |

---

## 4. API 엔드포인트

### AuthController (`/api/v1/auth`)

| HTTP | 경로 | 인증 | 기능 |
|---|---|---|---|
| POST | `/login` | 공개 | 소셜 로그인 |
| POST | `/refresh` | 공개 | 토큰 갱신 |
| POST | `/logout` | 필요 | 로그아웃 |

### MemberController (`/api/v1/members`)

| HTTP | 경로 | 인증 | 기능 |
|---|---|---|---|
| POST | `/register` | 필요 | 회원가입 |
| GET | `/me` | 필요 | 프로필 조회 |
| PATCH | `/me/nickname` | 필요 | 닉네임 변경 |

### DeparturePlaceController (`/api/v1/departure-places`)

| HTTP | 경로 | 인증 | 기능 |
|---|---|---|---|
| POST | `/` | 필요 | 출발지 추가 |
| GET | `/` | 필요 | 전체 조회 |
| PUT | `/{id}` | 필요 | 수정 |
| DELETE | `/{id}` | 필요 | 삭제 |
| PATCH | `/{id}/default` | 필요 | 기본 출발지 변경 |

### TermsController (`/api/v1/terms`)

| HTTP | 경로 | 인증 | 기능 |
|---|---|---|---|
| GET | `/` | 공개 | 약관 목록 |
| POST | `/agree` | 필요 | 약관 동의 |

### DeviceTokenController (`/api/v1/device-tokens`)

| HTTP | 경로 | 인증 | 기능 |
|---|---|---|---|
| POST | `/` | 필요 | 토큰 등록 |
| DELETE | `/` | 필요 | 토큰 삭제 |
