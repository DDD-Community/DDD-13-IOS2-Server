# Application Design — FC-12 보완: 이동경로 스냅샷 저장

> 2026-06-25. requirements-fc12-station-path.md 기반. 단일 단위(Units SKIP).

## 1. 도메인 변경

### 신규 값객체 `TravelPathPoint` (meeting.domain)
```
record TravelPathPoint(Long stationId, double latitude, double longitude)
```
- 경로 한 점. JSONB 직렬화 대상.

### `MeetingTravelBurden` 확장
- 필드 추가: `List<TravelPathPoint> stationPath`
- `of(...)` 팩토리에 `stationPath` 파라미터 추가.

## 2. 지하철 그래프 (subway.domain)

### `SubwayGraph.dijkstra` 반환 변경
- 기존: `Map<Long,int[]> dijkstra(source)`
- 변경: `DijkstraResult dijkstra(source)`
  - `record DijkstraResult(Map<Long,int[]> dist, Map<Long,Long> prev)`
  - `prev[stationId]` = 최단경로 상 직전 역 (source는 미포함/null).
- 신규 정적 메서드: `List<Long> reconstructPath(Map<Long,Long> prev, Long source, Long dest)`
  - dest==source → `[source]`
  - dest 도달 불가(prev에 없음) → `[]`
  - 그 외 → source…dest 순서 복원 리스트.

### 좌표 조회 (subway.domain / infra)
- `SubwayStationRepository.findCoordinatesByIds(List<Long> ids) : List<StationCoordinate>`
  - `record StationCoordinate(Long stationId, double latitude, double longitude)`
  - native: `SELECT station_id, latitude, longitude FROM subway_station WHERE station_id IN (:ids)`

## 3. 애플리케이션 서비스 `PlaceVoteService.computeAndSaveTravelBurdens`

흐름 (변경점 ⭐):
1. 그래프 미로드 → 생략 (동일)
2. 활성+좌표 참여자, 후보 최근접역 매핑 (동일)
3. 참여자별 `DijkstraResult` 1회 ⭐ (dist+prev)
4. 후보별:
   - seconds/transfers = dist 조회 (동일)
   - ⭐ `pathIds = reconstructPath(prev, source, dest)` → 중간 보관
5. ⭐ 전체 경로의 distinct stationId 수집 → `findCoordinatesByIds` 1회 배치 조회 → `Map<Long, coord>`
6. ⭐ pathIds → `List<TravelPathPoint>` 변환하여 `MeetingTravelBurden.of(... stationPath)` 생성
7. saveAll (동일)

- 도달 불가: pathIds 빈 리스트 → stationPath 빈 리스트.

## 4. 영속성

### V29 마이그레이션
```sql
ALTER TABLE meeting_travel_burden ADD COLUMN station_path JSONB;
COMMENT ON COLUMN meeting_travel_burden.station_path IS '출발역→도착역 경로 [{stationId,latitude,longitude}] 순서 리스트 (반정규화 스냅샷)';
```

### `MeetingTravelBurdenJpaEntity`
- `@JdbcTypeCode(SqlTypes.JSON) @Column(name="station_path", columnDefinition="jsonb") List<TravelPathPoint> stationPath`
- from/toDomain 매핑 추가 (PlaceJpaEntity JSON 매핑 선례 동일 패턴).

## 5. 표현 계층

### `PlaceTravelBurdenResponse.MemberBurden` 확장
- 필드 추가: `List<PathPoint> path`
  - `record PathPoint(Long stationId, double latitude, double longitude)`
- `getPlaceTravelBurden`에서 도메인 `stationPath` → `path` 매핑.

### `getVoteStatus`
- 변경 없음 (경로 미포함, 요약만 유지).

## 6. 영향 파일 목록
- 신규: `meeting/domain/TravelPathPoint.java`, `subway/domain/StationCoordinate.java`, `V29__add_travel_burden_station_path.sql`
- 수정: `SubwayGraph.java`, `SubwayStationRepository.java`, `SubwayStationRepositoryImpl.java`, `SubwayStationJpaRepository.java`, `MeetingTravelBurden.java`, `MeetingTravelBurdenJpaEntity.java`, `PlaceVoteService.java`, `PlaceTravelBurdenResponse.java`
