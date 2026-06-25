ALTER TABLE meeting_participant
    ADD COLUMN departure_label       VARCHAR(20),
    ADD COLUMN departure_place_name  VARCHAR(100),
    ADD COLUMN departure_address     VARCHAR(255);

COMMENT ON COLUMN meeting_participant.departure_label IS '출발지 별칭 (참여 당시, nullable)';
COMMENT ON COLUMN meeting_participant.departure_place_name IS '출발지 카카오 장소명 (nullable)';
COMMENT ON COLUMN meeting_participant.departure_address IS '출발지 주소 (도로명 우선, nullable)';
