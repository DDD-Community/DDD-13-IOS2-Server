# Services

---

## 1. AuthService (auth 패키지)

### socialLogin

```
1. SocialAuthClient로 공급자 토큰 검증 → SocialUserInfo 획득
2. MemberRepository에서 (provider + socialUserId)로 회원 조회
3. 없으면 → Member.create()로 신규 생성 + 저장
4. JwtProvider로 Access/Refresh Token 생성
5. RefreshToken 해시 저장
6. LoginResponse 반환 (tokens + isNewMember 플래그)
```

### refreshToken

```
1. refresh token 유효성 검증
2. 토큰 해시로 DB 조회 → 만료/폐기 확인
3. 기존 토큰 폐기 + 새 토큰 발급 + 저장
```

### logout

```
1. memberId로 모든 RefreshToken 폐기
```

---

## 2. MemberService (member 패키지)

### register

```
1. NicknameValidator로 금칙어 검증
2. TermsService.hasAgreedAllRequired() 확인 → 미동의 시 거부
3. Member 프로필 업데이트 (닉네임)
4. DeparturePlaceService.create()로 기본 출발지 등록
```

---

## 3. DeparturePlaceService (member 패키지)

### create
```
1. 최대 개수(10) 검증
2. isDefault=true면 기존 기본 출발지 해제
3. 저장
```

### delete
```
1. 소유권 검증 (IDOR 방지)
2. 기본 출발지면 삭제 거부
3. 삭제
```

---

## 4. TermsService (member 패키지)

### agreeTerms
```
1. termsIds로 약관 조회
2. 각 약관에 대해 TermsAgreement 생성 (중복 스킵)
3. 저장 (DELETE 금지 — INSERT만)
```

---

## 5. DeviceTokenService (member 패키지)

### register
```
1. 기존 토큰 조회 → 있으면 갱신, 없으면 생성
```

---

## 패키지 간 의존

| 호출자 | 피호출자 | 용도 |
|---|---|---|
| AuthService | MemberRepository | 회원 조회/생성 |
| MemberService | TermsService | 회원가입 시 약관 동의 확인 |
| MemberService | DeparturePlaceService | 회원가입 시 기본 출발지 등록 |

> auth → member 방향으로만 의존. member는 auth에 의존하지 않음.
