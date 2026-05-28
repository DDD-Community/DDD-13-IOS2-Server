CREATE TABLE meeting_participant (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id        BIGINT       NOT NULL REFERENCES meeting(id),
    member_id         BIGINT       NOT NULL REFERENCES member(id),
    latitude          DOUBLE PRECISION NOT NULL,
    longitude         DOUBLE PRECISION NOT NULL,
    attendance_status VARCHAR(10)  NOT NULL DEFAULT 'JOIN',
    CONSTRAINT uk_meeting_participant UNIQUE (meeting_id, member_id)
);

COMMENT ON TABLE meeting_participant IS '모임별 참여자 출발지 스냅샷 (location 단계 시작 시 생성)';
COMMENT ON COLUMN meeting_participant.latitude IS '출발지 위도';
COMMENT ON COLUMN meeting_participant.longitude IS '출발지 경도';
COMMENT ON COLUMN meeting_participant.attendance_status IS '참석 상태 (JOIN/LATE/ABSENT)';

CREATE INDEX idx_meeting_participant_meeting ON meeting_participant(meeting_id);
