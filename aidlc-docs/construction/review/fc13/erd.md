# ERD — FC-13 확정

```mermaid
erDiagram
    meeting ||--|| meeting_confirmed_place : has
    place ||--o{ meeting_confirmed_place : refers

    meeting_confirmed_place {
        bigint id PK
        bigint meeting_id FK UK
        bigint place_id FK
        varchar place_name "고정 저장"
        text address "고정 저장"
        timestamptz confirmed_at
    }
```
- V25: meeting_confirmed_place
- ⭐ 1~3위 rank 는 별도 테이블 없이 조회 시 계산(순위 비교자 공유). 동점 4순위 = 후보별 min(meeting_place_pick.picked_at)
- ⭐ 후보 집합·등록순서 소스 = meeting_place_pick (V26 source 컬럼, FC-12 참조)
