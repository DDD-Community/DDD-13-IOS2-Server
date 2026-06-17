CREATE TABLE meeting_travel_burden (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id  BIGINT  NOT NULL,
    member_id   BIGINT  NOT NULL,
    place_id    BIGINT  NOT NULL REFERENCES place(id),
    seconds     INT     NOT NULL,
    transfers   INT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_burden_meeting_member_place UNIQUE (meeting_id, member_id, place_id)
);

CREATE INDEX idx_burden_meeting_id ON meeting_travel_burden(meeting_id);

COMMENT ON TABLE meeting_travel_burden IS '이동부담 스냅샷 — 투표 시작 시 1회 Dijkstra 계산, Cloud Run 다중 인스턴스 안전';
COMMENT ON COLUMN meeting_travel_burden.seconds IS '출발지→장소 최근접역 소요초 (subway_edge 다익스트라)';
COMMENT ON COLUMN meeting_travel_burden.transfers IS '환승 횟수 (TRANSFER 엣지 통과 수)';
