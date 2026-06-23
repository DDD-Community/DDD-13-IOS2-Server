# ERD — 중간지점 역 후보 추출 (FC-midpoint)

```
erDiagram
    %% [기존] 회원
    member {
        BIGINT id PK "auto increment PK"
        VARCHAR provider "소셜 로그인 제공자"
        VARCHAR provider_id "소셜 고유 ID"
        VARCHAR nickname "닉네임"
        VARCHAR status "ACTIVE / INACTIVE"
    }

    %% [기존] 출발지
    departure_place {
        BIGINT id PK "auto increment PK"
        BIGINT member_id FK "member 참조"
        VARCHAR label "출발지 별칭"
        VARCHAR address "지번 주소"
        VARCHAR road_address "도로명 주소"
        VARCHAR place_name "카카오 장소명 (nullable)"
        DOUBLE latitude "위도"
        DOUBLE longitude "경도"
        BOOLEAN is_default "기본 출발지 여부"
    }

    %% [기존] 그룹
    group_info {
        BIGINT id PK "auto increment PK"
        VARCHAR name "그룹명"
        VARCHAR theme_tag_code FK "테마 태그 코드"
        VARCHAR status "ACTIVE / CLOSED"
    }

    %% [기존] 그룹 멤버
    group_member {
        BIGINT id PK "auto increment PK"
        BIGINT group_id FK "group_info 참조"
        BIGINT member_id FK "member 참조"
        VARCHAR role "HOST / MEMBER"
        VARCHAR attendance_status "JOIN / LATE / ABSENT"
    }

    %% [기존] 모임
    meeting {
        BIGINT id PK "auto increment PK"
        BIGINT group_id FK "group_info 참조"
        VARCHAR name "모임명"
        VARCHAR location_status "BEFORE / IN_PROGRESS / COMPLETED"
        VARCHAR date_vote_status "BEFORE / IN_PROGRESS / COMPLETED"
        VARCHAR status "ACTIVE / CLOSED"
    }

    %% [FC-midpoint 신규] 모임별 참여자 출발지 스냅샷
    meeting_participant {
        BIGINT id PK "auto increment PK"
        BIGINT meeting_id FK "meeting 참조"
        BIGINT member_id FK "member 참조"
        DOUBLE latitude "출발지 위도"
        DOUBLE longitude "출발지 경도"
        VARCHAR attendance_status "JOIN / LATE / ABSENT"
    }

    %% [FC-midpoint 신규] 지하철역 마스터 (데이터 직접 import)
    subway_station {
        BIGINT station_id PK "공공데이터 역사 ID"
        VARCHAR station_name "역명 (예: 홍대입구)"
        VARCHAR line_name "노선명 (예: 2호선)"
        DOUBLE latitude "역 위도"
        DOUBLE longitude "역 경도"
        GEOGRAPHY location_point "PostGIS 공간 인덱스용 (latitude/longitude 기반 자동 생성)"
    }

    %% [FC-midpoint 신규] 모임별 중간지점 역 후보
    midpoint_station_candidate {
        BIGINT id PK "auto increment PK"
        BIGINT meeting_id FK "meeting 참조"
        INT rank "후보 순위 (1/2/3)"
        BIGINT station_id FK "subway_station 참조 — 역 탭 키 (V27 추가)"
        VARCHAR station_name "역명"
        VARCHAR lines "노선 목록 (쉼표 구분)"
        NUMERIC distance_km "중심까지 거리 (km, 소수 3자리)"
        DOUBLE latitude "역 위도 — 지도 핀 (V27 추가)"
        DOUBLE longitude "역 경도 — 지도 핀 (V27 추가)"
    }

    member ||--o{ departure_place : "보유"
    member ||--o{ group_member : "소속"
    group_info ||--o{ group_member : "구성"
    group_info ||--o{ meeting : "포함"
    meeting ||--o{ meeting_participant : "참여자 스냅샷"
    member ||--o{ meeting_participant : "참여"
    meeting ||--o{ midpoint_station_candidate : "역 후보"
    subway_station ||--o{ midpoint_station_candidate : "역 마스터 참조"
```
