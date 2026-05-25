ALTER TABLE meeting
    ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE';

COMMENT ON COLUMN meeting.status IS '모임 진행 상태 (ACTIVE: 진행 중 / CLOSED: 종료됨)';
