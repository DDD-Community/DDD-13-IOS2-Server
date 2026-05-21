# API Documentation

## 공통 규칙
- Base URL: `/api/v1`
- 인증: `Authorization: Bearer {accessToken}` (공개 엔드포인트 제외)
- 에러 형식: `{ "code": "AUTH_001", "message": "..." }`

## Auth API

### POST /api/v1/auth/login
- **목적**: 소셜 로그인 (카카오/네이버/애플)
- **인증**: 불필요
- **Request**: `{ "provider": "KAKAO", "providerToken": "..." }`
- **Response**: `{ "accessToken", "refreshToken", "firstSocialLogin", "registrationCompleted" }`

### POST /api/v1/auth/refresh
- **목적**: Access/Refresh Token 갱신
- **인증**: 불필요
- **Request**: `{ "refreshToken": "..." }`

### POST /api/v1/auth/logout
- **목적**: 로그아웃 (모든 Refresh Token 폐기)
- **인증**: 필요

## Member API

### GET /api/v1/members/me
- **목적**: 내 프로필 조회

### PUT /api/v1/members/me
- **목적**: 회원가입 완료 (닉네임 설정)

### GET /api/v1/members/me/departure-places
- **목적**: 출발지 목록 조회

### POST /api/v1/members/me/departure-places
- **목적**: 출발지 등록

### DELETE /api/v1/members/me/departure-places/{id}
- **목적**: 출발지 삭제

## Data Models

### Member
- `id`: Long, `socialProvider`: KAKAO/NAVER/APPLE
- `nickname`: String(20), `profileImageUrl`: String
- `isRegistered`: boolean — 회원가입 완료 여부

### DeparturePlace
- `id`, `memberId`, `label`(10자), `address`(255자)
- `coordinate`: { latitude, longitude }
- `isDefault`: boolean

### ErrorCode 목록
| 코드 | HTTP | 의미 |
|---|---|---|
| AUTH_001 | 401 | 인증 필요 |
| AUTH_002 | 401 | 유효하지 않은 토큰 |
| AUTH_003 | 401 | 만료된 토큰 |
| MEMBER_001 | 404 | 회원 없음 |
| MEMBER_003 | 400 | 출발지 한도 초과 |
