CREATE TABLE midpoint_station_candidate (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id   BIGINT       NOT NULL REFERENCES meeting(id),
    rank         INT          NOT NULL,
    station_name VARCHAR(100) NOT NULL,
    lines        VARCHAR(200) NOT NULL,
    distance_km  NUMERIC(6,3) NOT NULL,
    CONSTRAINT uk_midpoint_candidate UNIQUE (meeting_id, rank)
);

COMMENT ON TABLE midpoint_station_candidate IS '모임별 중간지점 역 후보 (location 단계 시작 시 계산 저장)';
COMMENT ON COLUMN midpoint_station_candidate.rank IS '후보 순위 (1=가장 가까운 역)';
COMMENT ON COLUMN midpoint_station_candidate.lines IS '해당 역 노선 목록 쉼표 구분 (예: 2호선, 6호선)';
COMMENT ON COLUMN midpoint_station_candidate.distance_km IS '참여자 중심점까지 거리 (km)';

CREATE INDEX idx_midpoint_candidate_meeting ON midpoint_station_candidate(meeting_id);
