-- 모임 일정을 날짜+시간으로 확장 (DATE → TIMESTAMPTZ, 기존 LocalDateTime 컬럼 컨벤션과 동일)
ALTER TABLE meeting
    ALTER COLUMN confirmed_date TYPE TIMESTAMPTZ USING confirmed_date::timestamptz;
COMMENT ON COLUMN meeting.confirmed_date IS '확정된 모임 일시 (날짜+시간, 미확정 시 NULL)';

ALTER TABLE date_vote_option
    ALTER COLUMN candidate_date TYPE TIMESTAMPTZ USING candidate_date::timestamptz;
COMMENT ON COLUMN date_vote_option.candidate_date IS '후보 일시 (날짜+시간)';
