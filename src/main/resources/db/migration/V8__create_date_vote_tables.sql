-- date_vote_session: 날짜 투표 세션 (모임당 0~1개)
CREATE TABLE date_vote_session (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id    BIGINT       NOT NULL REFERENCES meeting(id),
    method        VARCHAR(10)  NOT NULL,
    deadline      DATE,
    duration_days INT,
    status        VARCHAR(10)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_date_vote_session_meeting UNIQUE (meeting_id)
);

COMMENT ON TABLE date_vote_session IS '날짜 투표 세션 (모임당 최대 1개)';
COMMENT ON COLUMN date_vote_session.method IS '투표 방식 (HOST_PICK/VOTE)';
COMMENT ON COLUMN date_vote_session.deadline IS '투표 마감일 (VOTE 방식만 유효)';
COMMENT ON COLUMN date_vote_session.duration_days IS '투표 기간 일수 (1/3/7, HOST_PICK 시 null)';
COMMENT ON COLUMN date_vote_session.status IS '세션 상태 (ACTIVE: 투표 진행 중 / EXPIRED: 마감됐으나 결정 불가 - 투표자 없음 또는 동률 / CONFIRMED: 날짜 확정됨)';

-- date_vote_option: 후보 날짜 (세션당 1~3개)
CREATE TABLE date_vote_option (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id     BIGINT  NOT NULL REFERENCES date_vote_session(id),
    candidate_date DATE    NOT NULL,
    sort_order     INT     NOT NULL
);

COMMENT ON TABLE date_vote_option IS '후보 날짜 (세션당 1~3개)';
COMMENT ON COLUMN date_vote_option.candidate_date IS '후보 날짜';
COMMENT ON COLUMN date_vote_option.sort_order IS '정렬 순서 (호스트 입력 순서, 0부터)';

-- date_vote_record: 투표 기록
CREATE TABLE date_vote_record (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    option_id BIGINT       NOT NULL REFERENCES date_vote_option(id),
    member_id BIGINT       NOT NULL REFERENCES member(id),
    voted_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_date_vote_record UNIQUE (option_id, member_id)
);

COMMENT ON TABLE date_vote_record IS '투표 기록 (option_id + member_id 중복 방지)';
COMMENT ON COLUMN date_vote_record.option_id IS '투표한 후보 날짜 ID';
COMMENT ON COLUMN date_vote_record.member_id IS '투표한 회원 ID';
