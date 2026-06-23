# API 명세 — FC-5 구성원 초대 및 합류

## 1. 초대 코드 발급

`POST /api/v1/groups/{groupId}/invite`

**인증**: JWT 필수 (HOST만)

| 파라미터 | 타입 | 설명 |
|---|---|---|
| groupId | Long | 그룹 ID |

**Response** `201 Created`

```json
{
  "inviteCode": "550e8400-e29b-41d4-a716-446655440000"
}
```

| 상태코드 | 설명 |
|---|---|
| 404 GROUP_002 | 그룹 없음 |
| 403 GROUP_003 | 그룹 멤버 아님 |
| 403 GROUP_004 | 호스트 아님 |

**비즈니스 규칙**
- 발급 시 기존 초대 코드 무효화 후 재발급
- 유효기간: 발급 후 48시간

---

## 2. 초대 코드로 합류

`POST /api/v1/groups/join`

**인증**: JWT 필수

**Request Body**

```json
{
  "inviteCode": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response** `200 OK` (body 없음)

| 상태코드 | 설명 |
|---|---|
| 403 MEMBER_006 | 회원가입(기본 출발지 등록) 미완료 — `is_registered=false` |
| 404 GROUP_006 | 유효하지 않은 초대 코드 |
| 400 GROUP_007 | 만료된 초대 코드 |
| 400 GROUP_008 | 이미 이 그룹의 구성원 |
