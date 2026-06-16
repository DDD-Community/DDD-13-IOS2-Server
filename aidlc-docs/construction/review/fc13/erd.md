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
