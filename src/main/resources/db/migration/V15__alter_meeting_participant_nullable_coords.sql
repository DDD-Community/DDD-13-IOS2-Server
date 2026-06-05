ALTER TABLE meeting_participant ALTER COLUMN latitude  DROP NOT NULL;
ALTER TABLE meeting_participant ALTER COLUMN longitude DROP NOT NULL;

COMMENT ON COLUMN meeting_participant.latitude  IS '출발지 위도 (합류 시 기본 출발지 없으면 null)';
COMMENT ON COLUMN meeting_participant.longitude IS '출발지 경도 (합류 시 기본 출발지 없으면 null)';
