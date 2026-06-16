# API 명세 — FC-8 중간지역 산출 + 장소 추천

## POST /api/v1/meetings/{meetingId}/location/start
**설명**: 호스트가 장소 정하기 시작 — 중간역 3개 + 추천 15개 산출, RECOMMENDED 전이
**인증**: JWT 필수 / **권한**: 호스트

### 요청
```json
{ "radiusKm": 2, "reservable": true, "parking": null }
```
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| radiusKm | Number | ⬜ | 기본 2, 부족 시 4·6 자동 확대 |
| reservable | Boolean | ⬜ | 예약가능 하드필터(요청 시) |
| parking | Boolean | ⬜ | 주차가능 하드필터(요청 시) |

### 응답 (200)
추천 요약 또는 200 OK

### 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 403 | NOT_GROUP_HOST | 호스트 아님 |
| 400 | PLACE_PHASE_NOT_READY | 날짜 미확정 |
| 400 | LOCATION_PHASE_ALREADY_STARTED | 이미 시작 |
| 400 | PARTICIPANT_DEPARTURE_NOT_SET | 출발지 미등록 참여자 |
| 400 | MIDPOINT_STATION_NOT_FOUND | 6km까지 역 0개 |
| 400 | PLACE_RECOMMENDATION_EMPTY | 6km까지 장소 0개 |

## GET /api/v1/meetings/{meetingId}/recommendations
- 추천 15 조회: rank, placeId, name, categoryLabel, score, nearestStationId

## GET /api/v1/places/options
- 모임 생성 화면용: category 고정 11종 + vibe 표준목록(place.vibe distinct)
