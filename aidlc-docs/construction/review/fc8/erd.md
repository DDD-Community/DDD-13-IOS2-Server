# ERD — FC-8 추천

```mermaid
erDiagram
    meeting ||--o{ meeting_place_recommendation : has
    place ||--o{ meeting_place_recommendation : refers
    subway_station ||--o{ meeting_place_recommendation : nearest

    place {
        bigint id PK
        bigint place_id UK "네이버"
        varchar category_label
        text_array vibe
        text_array occasion "%% [기존] AI 용도 태그(예: 회식/가족모임/스터디) — theme_tag.display_name과 직접 비교, 신규 컬럼 없음"
        boolean reservable
        boolean has_parking
        numeric rating
        geography location_point
    }
    meeting_place_recommendation {
        bigint id PK
        bigint meeting_id FK
        bigint place_id FK
        int rank "1..15"
        double score
        bigint nearest_station_id FK "귀속역"
        timestamptz created_at
    }
```
- place 테이블 변경 없음 — 기존 `occasion`(TEXT[], V12) 그대로 사용
- V20: meeting_place_recommendation 신규
- (meeting + categories/vibes/reservable/parking 는 FC-4 erd 참조, V18)
