# API 명세 — FC-8 중간지역 산출 + 장소 추천

## POST /api/v1/meetings/{meetingId}/location/start
**설명**: 호스트가 장소 정하기 시작 — 중간역 3개 + 추천 15개 산출, RECOMMENDED 전이
**인증**: JWT 필수 / **권한**: 호스트

### 요청
```json
{ "radiusKm": 2 }
```
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| radiusKm | Number | ⬜ | 기본 2, 부족 시 4·6 자동 확대, 6 초과 시 400 |

> 예약가능/주차가능 하드필터는 **모임 생성 시 입력한 `reservable`/`parking` 값**(FC-4 참조)을 그대로 사용한다. 이 API에서는 입력받지 않음.

### 응답 (200)
추천 요약 또는 200 OK

### 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 403 | NOT_GROUP_HOST | 호스트 아님 |
| 400 | INVALID_INPUT | radiusKm이 6 초과 |
| 400 | PLACE_PHASE_NOT_READY | 날짜 미확정 |
| 400 | LOCATION_PHASE_ALREADY_STARTED | 이미 시작 |
| 400 | PARTICIPANT_DEPARTURE_NOT_SET | 출발지 미등록 참여자 |
| 400 | MIDPOINT_STATION_NOT_FOUND | 6km까지 역 0개 |
| 400 | PLACE_RECOMMENDATION_EMPTY | 6km까지 장소 0개 |

## GET /api/v1/meetings/{meetingId}/recommendations
- 추천 15 조회: `rank`, `place`(PlaceSummary), `score`, `nearestStationId`
- 장소 정보는 평탄 필드가 아니라 `place` 객체로 내려감
```json
[
  { "rank": 1, "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 }, "score": 0.87, "nearestStationId": 240 }
]
```

## PlaceSummary (공통)
- placeId만 내려주던 응답에 임베드하는 표시용 공통 객체. 모든 장소 목록/후보/담기 응답에서 동일하게 사용
- 지도 표시용 `latitude`/`longitude` 포함 — 목록만으로 핀을 찍을 수 있어 별도 상세 조회 없이 동작
```json
{ "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 }
```
- vibe·예약/주차·평점 등 **풀 상세는 `GET /api/v1/places?ids=...`** 일괄 조회 API로 조회

## GET /api/v1/places?ids={id1,id2,...}
- 장소 상세 **1~N건 일괄 조회**: `ids` 쿼리파라미터로 placeId 목록 전달 → `PlaceDetailResponse` 배열 반환
- 각 항목: `placeId, name, categoryLabel, address, latitude, longitude, vibe[], occasion[], reservable, hasParking, rating`
- **요청 순서 보존**, 존재하지 않는 placeId는 결과에서 제외(에러 아님). id 1개만 넘기면 단건 조회로 동작
```json
[
  { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...",
    "latitude": 37.5, "longitude": 127.0, "vibe": ["분위기좋은"], "occasion": ["회식"],
    "reservable": true, "hasParking": false, "rating": 4.3 }
]
```

## GET /api/v1/places/options
- 모임 생성 화면용: category 고정 11종 + vibe 표준목록(place.vibe distinct)
