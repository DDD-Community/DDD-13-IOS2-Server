ALTER TABLE meeting_travel_burden ADD COLUMN station_path JSONB;

COMMENT ON COLUMN meeting_travel_burden.station_path IS '출발역→도착역 경로 [{stationId,latitude,longitude}] 순서 리스트 (반정규화 스냅샷, 친구들 거리보기 지도 표시용)';
