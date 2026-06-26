# 비즈니스 규칙 — 중간지점 역 후보 추출

## location 단계 시작 규칙
- meeting.locationStatus 가 BEFORE 일 때만 시작 가능 (이미 IN_PROGRESS/COMPLETED면 에러)
- 호스트만 시작 가능

### 계산 규칙
- meeting_participant 에서 attendance_status != 'ABSENT' 인 참여자 좌표만 사용
- PostGIS ST_Centroid + ST_Collect 로 기하학적 중심 계산
- subway_station 에서 중심 2km 이내, 역명 기준 그룹화(노선 통합), 거리순 상위 3개
- 결과를 midpoint_station_candidate (rank 1~3) 에 저장

### 인가 규칙
- POST /meetings/{id}/location/start — HOST 역할만
- GET /meetings/{id}/midpoint-stations — 그룹 구성원이면 누구나

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| location 단계 시작 | locationStatus = BEFORE 일 때만 | 400 LOCATION_PHASE_ALREADY_STARTED |
| 권한 | 호스트만 | 403 NOT_GROUP_HOST |
| 출발지 없는 참여자 | ABSENT 제외 후 좌표 없으면 에러 | 400 PARTICIPANT_DEPARTURE_NOT_SET |
| 근처 역 없음 | 2km 이내 subway_station 없으면 에러 | 400 MIDPOINT_STATION_NOT_FOUND |

## meeting_participant 참여자 규칙
- meeting_participant 행은 그룹 생성/합류 시점에 생성됨 (attendance_status 기본 JOIN)
- 좌표 집계는 meeting_participant(attendance_status != ABSENT) 기준 — 참석여부는 group_member가 아닌 meeting_participant에서 관리
- 각 멤버의 is_default=true departure_place 좌표 사용
- is_default departure_place 없는 멤버 존재 시 에러
