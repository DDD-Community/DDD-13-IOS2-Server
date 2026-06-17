# API 명세 — FC-9 장소 담기

## GET /api/v1/meetings/{meetingId}/places

**인증**: JWT 필수  
**권한**: 그룹 구성원

### 요청 쿼리 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| stationId | Long | N | 역 탭 필터 (nearestStationId) |
| category | String | N | 카테고리 필터 (예: RESTAURANT) |
| reservable | Boolean | N | true = 예약가능한 곳만 |
| parking | Boolean | N | true = 주차가능한 곳만 |

### 응답 (200 OK)

> cardDistance는 U4 SubwayGraph 연동 후 채워짐 (현재 null)

### 에러
| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 404 | MEETING_NOT_FOUND | 모임 없음 |
| 403 | NOT_GROUP_MEMBER | 그룹 구성원 아님 |

---

## POST /api/v1/meetings/{meetingId}/places/{placeId}/pick

**인증**: JWT 필수  
**권한**: 그룹 구성원

### 응답 (204 No Content)

### 에러
| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 400 | LOCATION_NOT_RECOMMENDED | locationStatus != RECOMMENDED |
| 400 | PLACE_PICK_CLOSED | 담기 마감 경과 |
| 403 | NOT_GROUP_MEMBER | 그룹 구성원 아님 |

---

## DELETE /api/v1/meetings/{meetingId}/places/{placeId}/pick

**인증**: JWT 필수  
**권한**: 그룹 구성원

### 응답 (204 No Content)
없는 담기 취소는 무시(no-op)

### 에러
| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 400 | LOCATION_NOT_RECOMMENDED | locationStatus != RECOMMENDED |
| 400 | PLACE_PICK_CLOSED | 담기 마감 경과 |

---

## GET /api/v1/meetings/{meetingId}/places/pick-status

**인증**: JWT 필수  
**권한**: 그룹 구성원

### 응답 (200 OK)


---

## POST /api/v1/meetings/{meetingId}/place-vote

**인증**: JWT 필수  
**권한**: 호스트만

### 요청

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| durationDays | Number | Y | 1 / 3 / 7 중 하나 |

### 응답 (200 OK)

### 에러
| 상태코드 | 에러코드 | 발생 조건 |
|---|---|---|
| 400 | LOCATION_NOT_RECOMMENDED | RECOMMENDED 아닌 상태 또는 후보 0개 |
| 400 | INVALID_DURATION_DAYS | 1·3·7 외 값 |
| 400 | PLACE_VOTE_DEADLINE_INVALID | 마감일 >= 약속 날짜 |
| 403 | NOT_GROUP_HOST | 호스트 아님 |
