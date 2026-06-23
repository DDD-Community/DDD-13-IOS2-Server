# FC-midpoint 처리 흐름

---

## 상태 전이

```
LocationStatus:

BEFORE ──[POST /location/start (HOST)]──→ IN_PROGRESS
```

- `BEFORE`가 아닐 때 start 호출 → `LOCATION_PHASE_ALREADY_STARTED (400)`

---

## 1. 장소 선정 단계 시작

`POST /api/v1/meetings/{meetingId}/location/start`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. groupMember 조회 → HOST 확인 (아니면 403)
4. `meeting.locationStatus == BEFORE` 확인 (아니면 400)
5. `meeting.locationStatus = IN_PROGRESS`
6. meeting_participant 조회 (meetingId 기준) → ABSENT 제외 필터
7. 참여자 중 lat/lng null인 사람 있으면 400 (출발지 미설정)
8. PostGIS 쿼리 실행
   - meeting_participant lat/lng → `ST_Collect` → `ST_Centroid` → center_point
   - subway_station 중 `ST_DWithin(center_point, 2000m)` 필터
   - station_name 기준 GROUP BY, line_name `string_agg`, `MIN(station_id)`, `MIN(latitude/longitude)`
   - dist_m ASC 정렬, LIMIT 3
   - 결과 없으면 400
9. midpoint_station_candidate 저장 (rank 1~3) — **station_id / latitude / longitude 포함**
   - `station_id` = 위 `MIN(station_id)` (장소 검색에 넘기는 stationIds 와 동일 값 → `meeting_place_recommendation.nearest_station_id` 와 정확히 매핑)
10. meeting 저장

> meeting_participant는 이 시점에 생성하지 않음 — 합류 시 이미 생성된 레코드를 조회만 함

---

## 2. 중간지점 역 후보 조회

`GET /api/v1/meetings/{meetingId}/midpoint-stations`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. groupMember 조회 → 멤버 확인 (아니면 403)
4. midpoint_station_candidate 조회 (rank ASC)
5. MidpointStationCandidateResponse 반환

---

## 연관 테이블 읽기/쓰기

| 테이블 | start | get |
|---|---|---|
| `meeting` | locationStatus 업데이트 | 읽기 (groupId 참조) |
| `group_member` | 읽기 (참여자 수집 + HOST 검증) | 읽기 (멤버 검증) |
| `departure_place` | 읽기 (기본 출발지 조회) | — |
| `meeting_participant` | **INSERT** | — |
| `subway_station` | 읽기 (PostGIS 쿼리) | — |
| `midpoint_station_candidate` | **INSERT** | **SELECT** |
