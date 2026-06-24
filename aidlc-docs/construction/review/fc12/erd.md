# ERD — FC-12 투표 + 이동부담

```mermaid
erDiagram
    meeting_place_vote_session ||--o{ meeting_place_vote : has
    place ||--o{ meeting_place_vote : voted
    member ||--o{ meeting_place_vote : casts
    meeting ||--o{ meeting_travel_burden : has
    subway_edge }o--|| subway_station : connects

    meeting_place_vote {
        bigint id PK
        bigint session_id FK
        bigint member_id FK "익명 집계용"
        bigint place_id FK
        timestamptz voted_at
    }
    meeting_travel_burden {
        bigint id PK
        bigint meeting_id FK
        bigint member_id FK
        bigint place_id FK
        int seconds "소요초"
        int transfers "환승수"
    }
```
- V23: meeting_place_vote (UNIQUE session_id, member_id, place_id)
- V24: meeting_travel_burden (UNIQUE meeting_id, member_id, place_id)
- subway_edge(V17, 기존): 그래프 원본 — 부팅 시 메모리 로드
- ⭐ V28: `meeting_place_pick` 에 `source VARCHAR(10) DEFAULT 'USER'` 추가 + `member_id` nullable
  - 투표 후보 집합 = meeting_place_pick 전체(USER+SYSTEM)
  - 담기 현황/함께담기 N/완료 구성원 = source='USER' 만 (FC-9 참조)
