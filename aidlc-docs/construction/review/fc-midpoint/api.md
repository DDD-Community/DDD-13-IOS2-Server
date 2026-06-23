# API 명세 — FC-midpoint (중간지점 역 후보)

## 1. 장소 선정 단계 시작

| 항목 | 값 |
|---|---|
| Method | POST |
| URL | `/api/v1/meetings/{meetingId}/location/start` |
| Auth | Bearer JWT (호스트만 허용) |
| Request Body | 없음 |
| Response | 200 OK (body 없음) |

### 처리 흐름 요약
1. meetingId로 모임 조회
2. 요청자가 해당 그룹의 HOST인지 검증
3. `meeting.locationStatus` BEFORE → IN_PROGRESS 전환
4. 그룹 멤버 중 ABSENT가 아닌 참여자 수집
5. 각 참여자의 기본 출발지 조회 → `meeting_participant` 저장
6. PostGIS 쿼리로 중간지점 역 후보 3개 계산
7. `midpoint_station_candidate` 저장 (rank 1~3)

### 에러 케이스

| 상황 | HTTP | ErrorCode | 메시지 |
|---|---|---|---|
| 모임 없음 | 404 | MEETING_001 | 모임을 찾을 수 없습니다 |
| 그룹 미가입 | 403 | GROUP_001 | 그룹 멤버가 아닙니다 |
| 호스트 아님 | 403 | MEETING_009 | 호스트만 수행할 수 있습니다 |
| 이미 시작됨 | 400 | MEETING_010 | 장소 선정이 이미 시작되었습니다 |
| 출발지 미설정 참여자 | 400 | MEETING_011 | 출발지를 설정하지 않은 참여자가 있습니다 |
| 주변 역 없음 | 400 | MEETING_012 | 중간지점 근처에 지하철역이 없습니다 |

---

## 2. 중간지점 역 후보 조회

| 항목 | 값 |
|---|---|
| Method | GET |
| URL | `/api/v1/meetings/{meetingId}/midpoint-stations` |
| Auth | Bearer JWT (그룹 멤버) |
| Request Body | 없음 |
| Response | 200 OK |

### Response Body 예시

```json
{
  "candidates": [
    {
      "rank": 1,
      "stationId": 1001,
      "stationName": "홍대입구",
      "lines": "2호선, 공항철도",
      "distanceKm": 0.842,
      "latitude": 37.5571,
      "longitude": 126.9245
    },
    {
      "rank": 2,
      "stationId": 1052,
      "stationName": "합정",
      "lines": "2호선, 6호선",
      "distanceKm": 1.205,
      "latitude": 37.5495,
      "longitude": 126.9136
    },
    {
      "rank": 3,
      "stationId": 1066,
      "stationName": "상수",
      "lines": "6호선",
      "distanceKm": 1.631,
      "latitude": 37.5478,
      "longitude": 126.9227
    }
  ]
}
```

> `stationId`는 `meeting_place_recommendation.nearestStationId` 와 매핑되는 **역 탭 키**다. FC-9 `GET /places?stationId=` 필터에 그대로 전달한다.
> `latitude`/`longitude`는 지도 핀 표시용.

### 에러 케이스

| 상황 | HTTP | ErrorCode | 메시지 |
|---|---|---|---|
| 모임 없음 | 404 | MEETING_001 | 모임을 찾을 수 없습니다 |
| 그룹 미가입 | 403 | GROUP_001 | 그룹 멤버가 아닙니다 |
