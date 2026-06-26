-- 참석여부는 미팅별(meeting_participant.attendance_status)로 관리한다.
-- 그룹 레벨 attendance_status 컬럼은 더 이상 사용하지 않으므로 제거한다.
ALTER TABLE group_member DROP COLUMN attendance_status;
