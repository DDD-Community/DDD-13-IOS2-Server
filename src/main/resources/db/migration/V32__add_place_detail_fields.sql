-- 장소 상세 바텀시트(FC-8 보강)용 컬럼 추가
ALTER TABLE place ADD COLUMN IF NOT EXISTS road_address   TEXT;
ALTER TABLE place ADD COLUMN IF NOT EXISTS business_hours TEXT;
ALTER TABLE place ADD COLUMN IF NOT EXISTS holiday        TEXT;

COMMENT ON COLUMN place.address        IS '지번주소 (V32: 의미 변경 도로명→지번)';
COMMENT ON COLUMN place.road_address   IS '도로명주소';
COMMENT ON COLUMN place.business_hours IS '영업시간 표시용 원문';
COMMENT ON COLUMN place.holiday        IS '휴무 표시용 원문';
