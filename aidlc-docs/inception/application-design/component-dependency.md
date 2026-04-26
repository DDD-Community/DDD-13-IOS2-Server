# Component Dependencies

---

## 패키지 의존 방향

```
global  ←──  auth  ──→  member
              │
              └── (외부) Kakao/Naver/Apple API
```

- `auth` → `member`: AuthService가 MemberRepository 사용 (회원 조회/생성)
- `auth` → `global`: JwtProvider 사용
- `member` → `global`: 공용 값객체, 에러 처리
- `member`는 `auth`에 의존하지 않음

---

## 레이어 규칙

```
Presentation → Application → Domain ← Infrastructure
```

| 규칙 | 설명 |
|---|---|
| Domain은 아무것도 의존하지 않음 | 순수 비즈니스 로직 |
| Application은 Domain만 의존 | Repository 인터페이스 사용 |
| Infrastructure는 Domain을 구현 | JPA Entity, Repository 구현체 |
| Presentation은 Application만 의존 | Controller → Service |

---

## 외부 의존성

| 외부 시스템 | 사용 컴포넌트 | 통신 |
|---|---|---|
| Kakao API | KakaoAuthClient | HTTPS |
| Naver API | NaverAuthClient | HTTPS |
| Apple JWKS | AppleAuthClient | HTTPS |
| PostgreSQL + PostGIS | JpaRepository들 | JDBC |

---

## 구현 순서

| 순서 | 대상 | 이유 |
|---|---|---|
| 1 | global | JWT, Security, Error Handling — 모든 API의 기반 |
| 2 | auth | 인증이 다른 기능의 전제 |
| 3 | member | auth에 의존, 나머지 기능 전부 |
