CREATE TABLE meeting_confirmed_place (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id   BIGINT       NOT NULL UNIQUE REFERENCES meeting(id),
    place_id     BIGINT       NOT NULL REFERENCES place(id),
    place_name   VARCHAR(200) NOT NULL,
    address      TEXT         NOT NULL,
    confirmed_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE meeting_confirmed_place IS '확정 장소 — 자동 순위(득표>이동시간합>환승합>등록순) 1위, 장소정보 고정 저장';
