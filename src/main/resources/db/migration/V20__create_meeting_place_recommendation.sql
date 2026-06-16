CREATE TABLE meeting_place_recommendation (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id         BIGINT           NOT NULL REFERENCES meeting(id),
    place_id           BIGINT           NOT NULL REFERENCES place(id),
    rank               INT              NOT NULL,
    score              DOUBLE PRECISION NOT NULL,
    nearest_station_id BIGINT           NOT NULL REFERENCES subway_station(station_id),
    created_at         TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE meeting_place_recommendation IS 'FC-8 장소 추천 스냅샷 (모임당 상위 15건)';
COMMENT ON COLUMN meeting_place_recommendation.rank IS '추천 순위 1~15 (점수 내림차순)';
COMMENT ON COLUMN meeting_place_recommendation.score IS 'SOFT 스코어 (0~1, 0.5occasion+0.25category+0.15vibe+0.1rating)';
COMMENT ON COLUMN meeting_place_recommendation.nearest_station_id IS '중간역 후보 3개 중 최근접역 (FC-9 역탭 필터용)';

CREATE INDEX idx_meeting_place_recommendation_meeting ON meeting_place_recommendation(meeting_id);
