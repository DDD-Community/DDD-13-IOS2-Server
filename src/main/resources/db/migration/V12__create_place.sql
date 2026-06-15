CREATE TABLE place (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    place_id       BIGINT           NOT NULL UNIQUE,
    name           VARCHAR(100)     NOT NULL,
    branch         VARCHAR(50),
    category       VARCHAR(100),
    category_label VARCHAR(20),   -- 한식/일식/중식/양식/카페/디저트/주점/분식/아시아음식/기타
    address        TEXT,
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    location_point geography(Point, 4326),
    has_room       BOOLEAN,  -- TRUE=있음 / FALSE=없음 / NULL=정보없음
    has_group_seat BOOLEAN,
    has_parking    BOOLEAN,
    reservable     BOOLEAN,
    max_group_size INT,
    vibe           TEXT[],
    occasion       TEXT[],
    size_fit       VARCHAR(2),
    summary        TEXT,
    naver_url      VARCHAR(200),
    rating         NUMERIC(3,2),
    review_count   INT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE place IS '모임 장소 마스터 (네이버 place_id 기준)';
COMMENT ON COLUMN place.place_id IS '네이버 place_id';
COMMENT ON COLUMN place.location_point IS 'PostGIS geography — ST_SetSRID(ST_MakePoint(longitude,latitude),4326)';
COMMENT ON COLUMN place.vibe IS 'AI 분위기 태그 배열 (예: {감성적,차분한})';
COMMENT ON COLUMN place.occasion IS 'AI 용도 태그 배열 (예: {친구모임,데이트})';
COMMENT ON COLUMN place.size_fit IS '소/중/대';

CREATE INDEX idx_place_location ON place USING GIST(location_point);
CREATE INDEX idx_place_vibe     ON place USING GIN(vibe);
CREATE INDEX idx_place_occasion ON place USING GIN(occasion);
