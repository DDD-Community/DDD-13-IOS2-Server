CREATE TABLE meeting_place_vote_session (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id  BIGINT       NOT NULL UNIQUE REFERENCES meeting(id),
    started_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deadline    TIMESTAMPTZ  NOT NULL,
    status      VARCHAR(12)  NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT ck_vote_session_status CHECK (status IN ('IN_PROGRESS', 'CLOSED'))
);

COMMENT ON TABLE meeting_place_vote_session IS '장소 투표 세션 — 모임당 1개, 마감일·상태 관리';
