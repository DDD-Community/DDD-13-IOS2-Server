-- FC-8 occasion 매칭 정합: place.occasion 실데이터(2299건) 기준 그룹모임에 맞는 고빈도 값에 맞춰 theme_tag 정비
-- 데이터 출처: pipeline/output/place_export.csv 전수 분석
-- 혼밥/혼술 등 "혼자" 용도 태그는 그룹모임 앱 취지와 안 맞아 제외 (occasion 원본 데이터 자체 정제는 별도 데이터 파이프라인 작업, 범위 외)
-- theme_tag는 코드 재배포 없이 데이터만으로 추가/변경 가능 (V7 주석 참조)

-- SOCIAL: '친목' -> '친구모임' (occasion 최다빈도 1943건과 동일 표기로 정렬)
UPDATE theme_tag SET display_name = '친구모임' WHERE code = 'SOCIAL';

INSERT INTO theme_tag (code, display_name, sort_order) VALUES
    ('DATE',            '데이트',   9),    -- 793건
    ('GROUP_GATHERING', '단체모임', 10),   -- 780건
    ('CAFE_TIME',       '카페타임', 11),   -- 523건
    ('LUNCH',           '점심식사', 12),   -- 331건
    ('SPECIAL_DAY',     '특별한날', 13),   -- 271건
    ('ANNIVERSARY',     '기념일',   14),   -- 10건
    ('SECOND_ROUND',    '2차',      15);   -- 10건
