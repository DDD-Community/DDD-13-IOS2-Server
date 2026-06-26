# API 명세 — FC-7-1 내 정보 수정

## 1. 참석여부 수정 (미팅 단위로 이전)

> 그룹 레벨 `PATCH /api/v1/groups/{groupId}/members/me/attendance` 는 **삭제됨**.
> 참석여부는 `group_member.attendance_status`(V31 DROP)가 아니라 **미팅 레벨 `meeting_participant.attendance_status`** 로 관리한다.

### `PATCH /api/v1/meetings/{meetingId}/participants/me/attendance`

**인증**: JWT 필수 (Authorization: Bearer {token})

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| meetingId | Long | 모임 ID |

**Request Body**

```json
{
  "attendanceStatus": "LATE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| attendanceStatus | String (enum) | Y | JOIN / LATE / ABSENT |

**Response**

| 상태코드 | 설명 |
|---|---|
| 204 No Content | 수정 성공 (body 없음) |
| 400 COMMON_001 | attendanceStatus 값 누락 또는 유효하지 않은 값 |
| 401 AUTH_001 | JWT 없음 |
| 404 MEETING_001 | 모임 없음 |
| 404 MEETING_013 | meeting_participant 레코드 없음 (합류 안 된 상태) |

---

## 2. 출발지 추가

### `POST /api/v1/departure-places`

**인증**: JWT 필수

**Request Body**

```json
{
  "label": "집",
  "address": "서울 강남구 삼성동 159",
  "roadAddress": "서울 강남구 영동대로 513",
  "placeName": "카카오프렌즈 코엑스점",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "isDefault": true
}
```

| 필드 | 타입 | 필수 | 제약 | 설명 |
|---|---|---|---|---|
| label | String | Y | 최대 10자 | 출발지 별칭 |
| address | String | Y | - | 지번 주소 (카카오 `address_name`) |
| roadAddress | String | Y | - | 도로명 주소 (카카오 `road_address_name`) |
| placeName | String | N | - | 장소명 (카카오 `place_name`, nullable) |
| latitude | Double | Y | -90~90 | 위도 (카카오 `y`) |
| longitude | Double | Y | -180~180 | 경도 (카카오 `x`) |
| isDefault | Boolean | N | - | 기본 출발지 여부 (첫 등록 시 서버가 강제 true) |

**Response** `201 Created`

```json
{
  "id": 1,
  "label": "집",
  "address": "서울 강남구 삼성동 159",
  "roadAddress": "서울 강남구 영동대로 513",
  "placeName": "카카오프렌즈 코엑스점",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "isDefault": true
}
```

**Error**

| 상태코드 | 설명 |
|---|---|
| 400 MEMBER_003 | 출발지 최대 3개 초과 |
| 400 COMMON_001 | 필드 유효성 오류 |
| 401 AUTH_001 | JWT 없음 |

**비즈니스 규칙**
- 출발지가 0개인 경우 isDefault는 항상 true로 강제 설정
- isDefault=true 요청 시 기존 기본 출발지 자동 해제

---

## 3. 모임별 출발지 변경

### `PATCH /api/v1/meetings/{meetingId}/participants/me/departure`

**인증**: JWT 필수

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| meetingId | Long | 모임 ID |

**Request Body**

```json
{
  "departurePlaceId": 2
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| departurePlaceId | Long | Y | 적용할 departure_place ID (본인 소유여야 함) |

**Response**

| 상태코드 | 설명 |
|---|---|
| 200 OK | 변경 성공 (body 없음) |
| 401 AUTH_001 | JWT 없음 |
| 404 MEETING_001 | 모임 없음 |
| 404 MEMBER_005 | 출발지 없음 또는 타인 소유 |
| 404 MEETING_013 | meeting_participant 레코드 없음 (합류 안 된 상태) |

**비즈니스 규칙**
- `departure_place`가 아닌 `meeting_participant.latitude/longitude`를 업데이트
- location/start 전에만 변경하는 것이 의도이나 API 자체는 상태 제한 없음

---

## 4. 출발지 수정

### `PUT /api/v1/departure-places/{id}`

**인증**: JWT 필수

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| id | Long | 출발지 ID |

**Request Body**

```json
{
  "label": "회사",
  "address": "서울 중구 태평로1가 31",
  "roadAddress": "서울 중구 세종대로 110",
  "placeName": null,
  "latitude": 37.5640,
  "longitude": 126.9750
}
```

| 필드 | 타입 | 필수 | 제약 | 설명 |
|---|---|---|---|---|
| label | String | Y | 최대 10자 | 출발지 별칭 |
| address | String | Y | - | 지번 주소 (카카오 `address_name`) |
| roadAddress | String | Y | - | 도로명 주소 (카카오 `road_address_name`) |
| placeName | String | N | - | 장소명 (카카오 `place_name`, nullable) |
| latitude | Double | Y | -90~90 | 위도 |
| longitude | Double | Y | -180~180 | 경도 |

> isDefault는 수정 불가 (장소 정보만 변경)

**Response** `200 OK`

```json
{
  "id": 1,
  "label": "회사",
  "address": "서울 중구 태평로1가 31",
  "roadAddress": "서울 중구 세종대로 110",
  "placeName": null,
  "latitude": 37.5640,
  "longitude": 126.9750,
  "isDefault": true
}
```

**Error**

| 상태코드 | 설명 |
|---|---|
| 400 COMMON_001 | 필드 유효성 오류 |
| 401 AUTH_001 | JWT 없음 |
| 404 MEMBER_005 | 출발지 없음 또는 타인 소유 (존재 여부 노출 방지) |
