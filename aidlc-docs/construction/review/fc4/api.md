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
  "themeTagCode": "DINING",
  "categoryLabels": ["한식", "주점"],
  "vibes": ["왁자지껄", "넓은"],
  "reservable": true,
  "parking": null
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| name | String | ✅ | 1~30자, 공백 불가 |
| themeTagCode | String | ✅ | theme_tag.code 값 중 하나 (= 목적/occasion) |
| categoryLabels | String[] | ⬜ | **[FC-8 신규]** 고정 11 카테고리 내 값 |
| vibes | String[] | ⬜ | **[FC-8 신규]** place.vibe 표준목록 내 값 |
| reservable | Boolean | ⬜ | **[FC-8 신규]** 예약 가능한 곳만 추천. NULL=조건 없음 |
| parking | Boolean | ⬜ | **[FC-8 신규]** 주차 가능한 곳만 추천. NULL=조건 없음 |

> categoryLabels/vibes/reservable/parking 은 **모임(meeting)에 저장**되어 FC-8 추천 시(`location/start`) 자동으로 적용된다. 그룹 테이블엔 저장 안 함. (호스트가 장소 정하기를 시작할 때 다시 입력하지 않음)

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
  { "code": "BUSINESS",        "displayName": "비즈니스" },
  { "code": "SOCIAL",          "displayName": "친구모임" },
  { "code": "FAMILY",          "displayName": "가족모임" },
  { "code": "DINING",          "displayName": "회식" },
  { "code": "CASUAL_MEAL",     "displayName": "간단한 식사" },
  { "code": "STUDY",           "displayName": "스터디" },
  { "code": "BIRTHDAY",        "displayName": "생일파티" },
  { "code": "WEDDING",         "displayName": "청첩장 모임" },
  { "code": "DATE",            "displayName": "데이트" },
  { "code": "GROUP_GATHERING", "displayName": "단체모임" },
  { "code": "CAFE_TIME",       "displayName": "카페타임" },
  { "code": "LUNCH",           "displayName": "점심식사" },
  { "code": "SPECIAL_DAY",     "displayName": "특별한날" },
  { "code": "ANNIVERSARY",     "displayName": "기념일" },
  { "code": "SECOND_ROUND",    "displayName": "2차" }
]
```
> **[FC-8 정합, V19]** `SOCIAL`의 displayName이 `친목`→`친구모임`으로 변경되고 `DATE`/`GROUP_GATHERING`/`CAFE_TIME`/`LUNCH`/`SPECIAL_DAY`/`ANNIVERSARY`/`SECOND_ROUND` 7종이 추가됨 — place.occasion 실데이터 매칭률 향상 목적 (혼밥/혼술 등 "혼자" 용도 값은 제외)

### 에러

| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 401 | AUTH_001 | JWT 없음 또는 만료 |
