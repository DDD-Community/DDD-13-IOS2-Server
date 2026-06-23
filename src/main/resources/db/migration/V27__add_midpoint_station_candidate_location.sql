ALTER TABLE midpoint_station_candidate
    ADD COLUMN station_id BIGINT,
    ADD COLUMN latitude   DOUBLE PRECISION,
    ADD COLUMN longitude  DOUBLE PRECISION;

COMMENT ON COLUMN midpoint_station_candidate.station_id IS '역 마스터 ID — meeting_place_recommendation.nearest_station_id 와 조인되는 역 탭 키';
COMMENT ON COLUMN midpoint_station_candidate.latitude IS '역 위도 (지도 핀 표시용)';
COMMENT ON COLUMN midpoint_station_candidate.longitude IS '역 경도 (지도 핀 표시용)';

CREATE INDEX idx_midpoint_candidate_station ON midpoint_station_candidate(station_id);
