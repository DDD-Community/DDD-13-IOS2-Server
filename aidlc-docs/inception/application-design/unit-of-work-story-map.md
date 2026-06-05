# Unit of Work Story Map — 중간지점 역 후보 추출 (MVP2)

## FR → Unit 매핑

| Functional Requirement | Unit |
|---|---|
| FR-1: meeting_participant 테이블 + 모임 생성 시 자동 복사 | Unit 1 |
| FR-2: subway_station 테이블 DDL + PostGIS 쿼리 | Unit 2 |
| FR-3: midpoint_station_candidate 테이블 | Unit 3 |
| FR-4: location 단계 시작 API + 역 계산 로직 | Unit 3 |
| FR-5: 역 후보 조회 API | Unit 3 |

## 스토리 (User Stories SKIP → FR 기반)

| # | As a... | I want to... | So that... | Unit |
|---|---|---|---|---|
| S-1 | 시스템 | 모임 생성 시 참여자 출발지 스냅샷 저장 | 중간지점 계산에 사용 | Unit 1 |
| S-2 | 시스템 | 지하철역 공간 쿼리 실행 | 중간지점 역 후보 찾기 | Unit 2 |
| S-3 | 호스트 | 장소 선정 단계 시작 | 역 후보 3개 자동 계산/저장 | Unit 3 |
| S-4 | 그룹 멤버 | 중간지점 역 후보 조회 | 어느 지역에서 모일지 확인 | Unit 3 |
