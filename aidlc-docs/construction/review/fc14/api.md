# API 명세 — FC-14 회원 탈퇴

> 2026-08-24 신규.

---

## 1. 회원 탈퇴

```
DELETE /api/v1/members/me
```

### 요청

| 구분 | 이름 | 필수 | 설명 |
|---|---|---|---|
| Header | `Authorization` | O | `Bearer {accessToken}` |
| Header | `X-Apple-Authorization-Code` | X | Apple 연동 해제용 authorization code. Apple 로그인 회원만 전달 |

- Body 없음. Path에 memberId를 받지 않는다 (인증 principal 사용)
- **왜 헤더인가**: 쿼리 파라미터는 액세스 로그에 code가 평문으로 남고, DELETE 요청 바디는 일부 프록시에서 누락될 수 있어 헤더로 전달한다

#### iOS 클라이언트 요구사항

Apple 로그인 회원의 경우, 탈퇴 시점에 **Apple 재인증을 수행해 새 `authorizationCode` 를 발급받아 헤더로 전달**해야 한다. 서버는 Apple 토큰을 저장하지 않으므로(ID Token 디코딩 후 폐기) 클라이언트 전달 없이는 revoke가 불가능하다.

### 응답

| 코드 | 조건 | 바디 |
|---|---|---|
| `204 No Content` | 탈퇴 성공 | 없음 |
| `401 Unauthorized` | 미인증 / 이미 탈퇴한 회원의 토큰 | 표준 에러 |
| `404 Not Found` | `MEMBER_001` 회원을 찾을 수 없음 | 표준 에러 |
| `400 Bad Request` | `MEMBER_007` 이미 탈퇴한 회원 | 표준 에러 |

- Apple revoke 실패, GCS 이미지 삭제 실패는 **응답에 영향을 주지 않는다** (204 유지, 서버 warn 로그)

### 에러 코드

| 코드 | HTTP | 메시지 |
|---|---|---|
| `MEMBER_001` | 404 | 회원을 찾을 수 없습니다 |
| `MEMBER_007` | 400 | 이미 탈퇴한 회원입니다 *(신규)* |

---

## 2. 기존 API 동작 변경

### 2-1. 전역 — 인증 필터

모든 인증 필요 API에 적용된다.

| 변경 전 | 변경 후 |
|---|---|
| JWT 서명·만료만 검증 | JWT 검증 + **회원 상태가 `ACTIVE` 인지 확인** |
| 탈퇴 후에도 Access Token 만료(1시간)까지 통과 | 탈퇴 즉시 401 |

### 2-2. `GET /api/v1/meetings/{meetingId}/place-vote/participants`

탈퇴 회원의 닉네임·프로필이 노출되던 문제를 수정한다.

| 필드 | 변경 전 | 변경 후 |
|---|---|---|
| `name` | 탈퇴자도 저장값 그대로 반환 | 탈퇴자는 `null` |
| `profileImageUrl` | 동일 | 탈퇴자는 `null` |

> 기존 4개 API(`GroupService`, `MeetingDetailService`, `MeetingListService`, `DateVoteService`)는 이미 동일 가드가 적용되어 있어 변경 없음.

### 2-3. 탈퇴 회원이 포함된 조회 API 전반

| API | 탈퇴 회원 표시 |
|---|---|
| 그룹 구성원 목록 | `nickname`·`profileImageUrl` = `null` |
| 모임 상세 구성원 | 동일. 출발지 목록은 빈 배열(파기됨) |
| 투표 참여자 목록 | 동일. `departureName` = `null` |
| 친구들 거리보기 | 탈퇴자는 `seconds`·`transfers` = `null`, `path` = `[]` (스냅샷 파기됨) |

---

## 3. 변경 없는 API

| API | 사유 |
|---|---|
| 소셜 로그인 (`POST /api/v1/auth/login`) | `social_user_id` 치환으로 탈퇴자는 자동으로 신규 회원 생성 경로를 탄다 |
| 중간지점 조회 | `ST_Collect` 가 NULL 좌표를 자동 제외 |
| 장소 추천 / 담기 / 투표 | 집계 데이터 유지 정책이므로 영향 없음 |
