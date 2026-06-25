# ERD — FC-12 투표 + 이동부담

```mermaid
erDiagram
    meeting_place_vote_session ||--o{ meeting_place_vote : has
    place ||--o{ meeting_place_vote : voted
    member ||--o{ meeting_place_vote : casts
    meeting ||--o{ meeting_travel_burden : has
    meeting ||--o{ meeting_participant : has
    subway_edge }o--|| subway_station : connects

    meeting_participant {
        bigint id PK
        bigint meeting_id FK
        bigint member_id FK
        double latitude "출발 위도 nullable"
        double longitude "출발 경도 nullable"
        varchar attendance_status "JOIN/LATE/ABSENT"
        varchar departure_label "출발지 별칭 ⭐V30 nullable"
        varchar departure_place_name "카카오 장소명 ⭐V30 nullable"
        varchar departure_address "주소 ⭐V30 nullable"
    }

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
        jsonb station_path "경로[{stationId,lat,lng}] ⭐V29"
    }
```
- V23: meeting_place_vote (UNIQUE session_id, member_id, place_id)
- V24: meeting_travel_burden (UNIQUE meeting_id, member_id, place_id)
- subway_edge(V17, 기존): 그래프 원본 — 부팅 시 메모리 로드
- ⭐ V28: `meeting_place_pick` 에 `source VARCHAR(10) DEFAULT 'USER'` 추가 + `member_id` nullable
  - 투표 후보 집합 = meeting_place_pick 전체(USER+SYSTEM)
  - 담기 현황/함께담기 N/완료 구성원 = source='USER' 만 (FC-9 참조)
- ⭐ V29: `meeting_travel_burden` 에 `station_path JSONB` 추가 (이동경로 스냅샷, 반정규화)
  - 값 = `[{stationId, latitude, longitude}, ...]` 출발→도착 순서. 친구들 거리보기 지도 표시용.
- ⭐ V30: `meeting_participant` 에 출발지 메타 3컬럼 추가 (`departure_label`, `departure_place_name`, `departure_address`, 모두 nullable)
  - 출발지 이름을 쓰기 시점 스냅샷으로 직접 저장(좌표 역매칭 폐기). 기본 출발지 기준 best-effort 백필.
