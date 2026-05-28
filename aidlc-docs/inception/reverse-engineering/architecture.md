# System Architecture (MVP2 기준 갱신 — 2026-05-28)

## System Overview
Spring Boot 3.4.4 / Java 17, DDD 레이어 아키텍처. PostgreSQL 15 + PostGIS + JPA + Flyway.
모임 조율 서비스 (Bangawo): 그룹 생성 → 모임 생성 → 날짜 투표 → 장소 선정 → 모임 확정.

## Bounded Contexts

| Context | 역할 |
|---|---|
| auth | 소셜 로그인 (Kakao/Naver/Apple), JWT 발급 |
| member | 회원 정보, 출발지(departure_place) 관리, 약관 동의 |
| group | 그룹/그룹멤버 관리, 테마태그 |
| meeting | 모임 CRUD, 날짜투표, 스케줄러 |
| subway (신규 MVP2) | 지하철역 데이터, 중간지점 역 후보 계산 |
| global | 공통 설정, 보안, 예외 처리 |

## Layer Dependency
```
presentation → application → domain ← infrastructure
```

## Data Flow: 중간지점 역 후보 계산 (신규)
```
Client → GET /meetings/{id}/midpoint-stations
  → MeetingController (또는 SubwayController)
  → SubwayService
    → group_member JOIN departure_place (default) → centroid 계산 (PostGIS)
    → subway_station → 2km 이내 역 후보 → 상위 3개
  → StationCandidateResponse
```

## Key Infrastructure
- **PostGIS**: V1__init_postgis.sql 로 활성화 완료
- **departure_place**: latitude/longitude (DOUBLE 컬럼, PostGIS geometry 아님)
- **subway_station**: 미존재 → 신규 테이블 생성 필요 (PostGIS geometry 포함)
- **meeting_participant**: 미존재 → group_member + departure_place 활용

## Integration Points
- **External APIs**: Kakao/Naver/Apple OAuth
- **Database**: PostgreSQL 15 + PostGIS (Cloud SQL)
- **Subway Data**: 공공데이터 (역사_ID, 역사명, 호선, 위도, 경도) → Flyway로 import
