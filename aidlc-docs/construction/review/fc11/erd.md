# ERD — FC-11 투표 세션

```mermaid
erDiagram
    meeting ||--|| meeting_place_vote_session : has

    meeting_place_vote_session {
        bigint id PK
        bigint meeting_id FK UK
        timestamptz started_at
        timestamptz deadline "23:59:59"
        varchar status "IN_PROGRESS/CLOSED"
    }
```
- V22: meeting_place_vote_session
