# API 명세 — FC-6 모임 리스트

---

## GET /api/v1/meetings

**인증**: JWT 필수
**권한**: 로그인한 사용자 누구나 (본인 소속 그룹만 조회됨)

### 요청

없음 (쿼리 파라미터 없음)

### 응답 (200 OK)

```json
[
  {
    "groupId": 1,
    "meetingId": 1,
    "name": "팀 회식",
    "themeTagCode": "DINING",
    "themeTagDisplay": "회식",
    "listStatus": "IN_PROGRESS",
    "locationStatus": "BEFORE",
    "dateVoteStatus": "BEFORE",
    "locationAddress": null,
    "memberCount": 3,
    "members": [
      {
        "memberId": 1,
        "nickname": "홍길동",
        "profileImageUrl": "https://example.com/profile1.jpg",
        "attendanceStatus": "JOIN"
      },
      {
        "memberId": 2,
        "nickname": "김철수",
        "profileImageUrl": "https://example.com/profile2.jpg",
        "attendanceStatus": "LATE"
      }
    ]
  },
  {
    "groupId": 2,
    "meetingId": 3,
    "name": "스터디 모임",
    "themeTagCode": "STUDY",
    "themeTagDisplay": "스터디",
    "listStatus": "CLOSED",
    "locationStatus": "COMPLETED",
    "dateVoteStatus": "COMPLETED",
    "locationAddress": null,
    "memberCount": 1,
    "members": [
      {
        "memberId": 1,
        "nickname": "홍길동",
        "profileImageUrl": "https://example.com/profile1.jpg",
        "attendanceStatus": "JOIN"
      }
    ]
  }
]
```

### 응답 필드 설명

| 필드 | 타입 | 설명 |
|---|---|---|
| `groupId` | Long | 그룹 ID |
| `meetingId` | Long | 모임 ID |
| `name` | String | 모임명 (= 그룹명) |
| `themeTagCode` | String | 테마 태그 코드 (예: DINING) |
| `themeTagDisplay` | String | 테마 태그 표시명 (예: 회식) |
| `listStatus` | String | 목록 상태 (IN_PROGRESS / CONFIRMED / CLOSED) |
| `locationStatus` | String | 장소 선정 상태 (BEFORE / IN_PROGRESS / COMPLETED) |
| `dateVoteStatus` | String | 날짜 투표 상태 (BEFORE / IN_PROGRESS / COMPLETED) |
| `locationAddress` | String? | 장소 주소 — MVP2 미구현, 항상 null |
| `memberCount` | int | 구성원 수 |
| `members[].memberId` | Long | 구성원 회원 ID |
| `members[].nickname` | String? | 닉네임 (탈퇴 회원은 null) |
| `members[].profileImageUrl` | String? | 프로필 이미지 URL |
| `members[].attendanceStatus` | String | 참석여부 (JOIN / LATE / ABSENT) |

### 정렬 규칙

1. `listStatus`: IN_PROGRESS → CONFIRMED → CLOSED
2. 동일 status 내: 모임 생성일 내림차순 (최신 모임이 위)
3. `members`: 합류일 오름차순 (먼저 합류한 구성원이 앞)

### 에러

| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 401 | AUTH_001 | JWT 없음 또는 만료 |
