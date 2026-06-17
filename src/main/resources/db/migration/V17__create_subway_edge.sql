CREATE TABLE subway_edge (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    from_station_id BIGINT      NOT NULL REFERENCES subway_station(station_id),
    to_station_id   BIGINT      NOT NULL REFERENCES subway_station(station_id),
    weight_sec      INT         NOT NULL,
    edge_type       VARCHAR(10) NOT NULL,
    CONSTRAINT uk_subway_edge UNIQUE (from_station_id, to_station_id, edge_type),
    CONSTRAINT ck_subway_edge_type CHECK (edge_type IN ('RIDE', 'TRANSFER'))
);

COMMENT ON TABLE subway_edge IS '지하철 이동 그래프 엣지 (KTDB GTFS 기반) — 서버 부팅 시 인접리스트로 로드, 이동시간·환승 계산용';
COMMENT ON COLUMN subway_edge.weight_sec IS '엣지 가중치(초). RIDE=역간 소요시간, TRANSFER=환승 소요시간';
COMMENT ON COLUMN subway_edge.edge_type IS 'RIDE=같은 노선 인접역 승차 / TRANSFER=환승역 노선 간 이동';

CREATE INDEX idx_subway_edge_from ON subway_edge(from_station_id);
