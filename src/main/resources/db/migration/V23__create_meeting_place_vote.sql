CREATE TABLE meeting_place_vote (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id  BIGINT       NOT NULL REFERENCES meeting_place_vote_session(id),
    member_id   BIGINT       NOT NULL,
    place_id    BIGINT       NOT NULL REFERENCES place(id),
    voted_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_session_member_place UNIQUE (session_id, member_id, place_id)
);

CREATE INDEX idx_vote_session_id ON meeting_place_vote(session_id);
CREATE INDEX idx_vote_member_session ON meeting_place_vote(member_id, session_id);

COMMENT ON TABLE meeting_place_vote IS '장소 투표 — 익명 집계 (member_id 저장하나 UI에 개인 귀속 미노출)';
