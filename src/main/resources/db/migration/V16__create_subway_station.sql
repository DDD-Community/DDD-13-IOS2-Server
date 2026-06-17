CREATE TABLE subway_station (
    station_id    BIGINT       PRIMARY KEY,
    station_name  VARCHAR(100) NOT NULL,
    line_name     VARCHAR(100) NOT NULL,
    latitude      DOUBLE PRECISION NOT NULL,
    longitude     DOUBLE PRECISION NOT NULL,
    location_point geography(Point, 4326)
);

COMMENT ON TABLE subway_station IS '지하철역 마스터 (공공데이터 직접 import)';
COMMENT ON COLUMN subway_station.station_id IS '공공데이터 역사 ID';
COMMENT ON COLUMN subway_station.station_name IS '역명 (예: 홍대입구)';
COMMENT ON COLUMN subway_station.line_name IS '노선명 (예: 2호선)';
COMMENT ON COLUMN subway_station.location_point IS 'PostGIS geography — 데이터 import 시 ST_SetSRID(ST_MakePoint(longitude,latitude),4326) 로 채울 것';

CREATE INDEX idx_subway_station_location ON subway_station USING GIST(location_point);
