# Application Design — 반가워(Bangawo) 서버

> MVP 단순 구조: **auth** + **member** 2패키지

---

## 패키지 구조

```
com.bangawo
├── global/          # JWT, Security, Error Handling, 값객체
├── auth/            # 소셜 로그인, JWT 발급
└── member/          # 프로필, 출발지, 약관, 디바이스 토큰
```

각 패키지 내부: `domain / application / infrastructure / presentation`

---

## API 엔드포인트 (15개)

| HTTP | 경로 | 인증 | 기능 |
|---|---|---|---|
| POST | `/api/v1/auth/login` | 공개 | 소셜 로그인 |
| POST | `/api/v1/auth/refresh` | 공개 | 토큰 갱신 |
| POST | `/api/v1/auth/logout` | 필요 | 로그아웃 |
| POST | `/api/v1/members/register` | 필요 | 회원가입 |
| GET | `/api/v1/members/me` | 필요 | 프로필 조회 |
| PATCH | `/api/v1/members/me/nickname` | 필요 | 닉네임 변경 |
| POST | `/api/v1/departure-places` | 필요 | 출발지 추가 |
| GET | `/api/v1/departure-places` | 필요 | 출발지 전체 조회 |
| PUT | `/api/v1/departure-places/{id}` | 필요 | 출발지 수정 |
| DELETE | `/api/v1/departure-places/{id}` | 필요 | 출발지 삭제 |
| PATCH | `/api/v1/departure-places/{id}/default` | 필요 | 기본 출발지 변경 |
| GET | `/api/v1/terms` | 공개 | 약관 목록 |
| POST | `/api/v1/terms/agree` | 필요 | 약관 동의 |
| POST | `/api/v1/device-tokens` | 필요 | 토큰 등록 |
| DELETE | `/api/v1/device-tokens` | 필요 | 토큰 삭제 |

---

## 핵심 플로우

### 소셜 로그인
```
POST /auth/login {provider, providerToken}
→ 공급자별 토큰 검증 → 회원 조회/생성 → JWT 발급
→ {accessToken, refreshToken, firstSocialLogin, registrationCompleted}
```

### 회원가입
```
POST /members/register {nickname, departurePlaces, agreedTermsIds}
→ 금칙어 검증 → 약관 동의 확인 → 프로필 저장 → 기본 출발지 등록
```

---

## 의존 방향

```
global  ←──  auth  ──→  member
```

auth → member (회원 조회/생성), member는 auth에 의존하지 않음.

---

## 구현 순서

1. **global** — JWT, Security, Error Handling
2. **auth** — 소셜 로그인, 토큰 관리
3. **member** — 프로필, 출발지, 약관, 디바이스 토큰
