# API 명세 — FC-4 그룹 & 첫 모임 생성

---

## POST /api/v1/groups/create

**컨트롤러**: `GroupController`
**설명**: 모임 이름과 테마 태그를 입력하면 그룹 · 첫 번째 모임 · 호스트 멤버십이 한 번에 생성됩니다.
**인증**: JWT 필수
**권한**: 로그인한 사용자 누구나

### 요청

```json
{
  "name": "팀 회식",
  "themeTagCode": "DINING"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| name | String | ✅ | 1~30자, 공백 불가 |
| themeTagCode | String | ✅ | theme_tag.code 값 중 하나 |

### 응답 (201 Created)

```json
{
  "groupId": 1,
  "meetingId": 1,
  "name": "팀 회식",
  "themeTagCode": "DINING"
}
```

### 에러

| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 400 | GROUP_001 | 이름 30자 초과 또는 공백 |
| 401 | AUTH_001 | JWT 없음 또는 만료 |

---

## GET /api/v1/theme-tags

**컨트롤러**: `ThemeTagController`
**설명**: 그룹 생성 화면에서 선택 가능한 테마 태그 목록을 정렬 순서(sort_order)대로 반환합니다.
**인증**: JWT 필수
**권한**: 로그인한 사용자 누구나

### 요청

없음 (쿼리 파라미터 없음)

### 응답 (200 OK)

```json
[
  { "code": "BUSINESS",    "displayName": "비즈니스" },
  { "code": "SOCIAL",      "displayName": "친목" },
  { "code": "FAMILY",      "displayName": "가족모임" },
  { "code": "DINING",      "displayName": "회식" },
  { "code": "CASUAL_MEAL", "displayName": "간단한 식사" },
  { "code": "STUDY",       "displayName": "스터디" },
  { "code": "BIRTHDAY",    "displayName": "생일파티" },
  { "code": "WEDDING",     "displayName": "청첩장 모임" }
]
```

### 에러

| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 401 | AUTH_001 | JWT 없음 또는 만료 |
