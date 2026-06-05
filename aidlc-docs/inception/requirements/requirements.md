# Requirements — 중간지점 역 후보 추출 (MVP2 첫 번째 기능)

## Intent Analysis
- **User Request**: 모임 참여자들의 출발지 기반으로 기하학적 중심을 계산하고, 2km 이내 지하철역 중 거리순 상위 3개를 후보로 저장 및 조회
- **Request Type**: New Feature (새로운 도메인 도입 — subway 컨텍스트)
- **Scope Estimate**: Multiple Components (meeting, subway, member 컨텍스트 연동)
- **Complexity Estimate**: Complex (PostGIS 쿼리, 새 테이블 3개, 상태 전이 트리거)

---

## 결정 사항 요약

| 항목 | 결정 |
|---|---|
| 출발지 소스 | `meeting_participant` 신규 테이블 (모임별 출발지 스냅샷) |
| 출발지 없는 멤버 | 에러 반환 (회원가입 시 기본 출발지 필수이므로 정상 플로우에서 발생 안 함) |
| 역 정렬 기준 | 중심 거리순 (dist_m ASC) |
| 역 점수 컬럼 | 없음 — 추후 ALTER TABLE로 추가 |
| subway_station 데이터 | DDL만 생성, 데이터는 직접 import |
| 역 후보 저장 | location 단계 시작 시 1회 계산 + DB 저장 |
| 역 후보 조회 API | 있음 (GET /meetings/{meetingId}/midpoint-stations) |
| 역 후보 개수 | 서비스 상수 3개 |

---

## Functional Requirements

### FR-1: meeting_participant 테이블 도입
- meeting_participant (id, meeting_id, member_id, location_point GEOGRAPHY(Point,4326), attendance_status)
- 모임 생성 시 group_member의 모든 멤버를 자동 복사
  - location_point: 해당 멤버의 departure_place(is_default=true) 좌표를 PostGIS 포인트로 변환
  - attendance_status: group_member.attendance_status 그대로 복사
- 제약: UNIQUE(meeting_id, member_id)

### FR-2: subway_station 테이블 DDL
- 컬럼: station_id (PK), station_name VARCHAR, line_name VARCHAR, location_point GEOGRAPHY(Point,4326), latitude DOUBLE, longitude DOUBLE
- 점수 컬럼 없음 (추후 ALTER TABLE)
- Flyway V11 마이그레이션으로 테이블만 생성 (데이터 INSERT 없음)
- GIST 인덱스: location_point

### FR-3: midpoint_station_candidate 테이블
- 컬럼: id, meeting_id (FK → meeting), rank (1/2/3), station_name, lines (복수 노선 ", " 연결), distance_km NUMERIC(6,3)
- 제약: UNIQUE(meeting_id, rank)
- Flyway V12 마이그레이션

### FR-4: location 단계 시작 API
- POST /meetings/{meetingId}/location/start (호스트 전용)
- meeting.locationStatus BEFORE → IN_PROGRESS 전이
- 전이 시 중간지점 역 계산 + midpoint_station_candidate 저장 트리거
- 계산 로직:
  1. meeting_participant WHERE meeting_id = X AND attendance_status != 'ABSENT' 에서 location_point 수집
  2. ST_Centroid(ST_Collect(location_point)) 로 기하학적 중심 계산
  3. subway_station에서 중심 2km 이내 역, 역명 기준 그룹화(중복 노선 통합)
  4. dist_m ASC 정렬, 상위 3개 저장
- 오류: location_point 가 null인 참여자 존재 시 BusinessException(PARTICIPANT_DEPARTURE_NOT_SET)

### FR-5: 중간지점 역 후보 조회 API
- GET /meetings/{meetingId}/midpoint-stations
- midpoint_station_candidate 조회 → rank 순 반환
- Response: { candidates: [{ rank, stationName, lines, distanceKm }] }
- 미계산 상태(locationStatus = BEFORE)이면 빈 배열 반환

---

## Non-Functional Requirements

### NFR-1: 성능
- PostGIS 쿼리는 Cloud SQL에서 실행 (PostGIS 이미 활성화)
- subway_station.location_point에 GIST 인덱스 필수
- meeting_participant.location_point에도 GIST 인덱스

### NFR-2: 보안
- POST /meetings/{meetingId}/location/start — 호스트(HOST 역할)만 호출 가능
- GET /meetings/{meetingId}/midpoint-stations — 그룹 멤버만 조회 가능

### NFR-3: 데이터 일관성
- 역 후보는 location 단계 시작 시 1회 스냅샷 → 이후 출발지 변경 영향 없음
- midpoint_station_candidate는 INSERT-only (location 단계 재시작 불가 가정)

---

## Out of Scope (이번 기능에서 제외)
- MeetingDetailService 리팩토링 (meeting_participant 기반으로 변경) — 별도 태스크
- 출석 변경 API의 meeting_participant 연동 — 별도 태스크
- 모임별 출발지 변경 API (PUT /meetings/{meetingId}/participants/my-departure) — 별도 태스크
- subway_station 데이터 import — 사용자 직접 처리

---

## 신규 DB 스키마 (3개 테이블)

```
meeting_participant
  id, meeting_id (FK), member_id (FK), location_point (GEOGRAPHY), attendance_status

subway_station
  station_id, station_name, line_name, location_point (GEOGRAPHY), latitude, longitude

midpoint_station_candidate
  id, meeting_id (FK), rank, station_name, lines, distance_km
```

## 신규 API (2개)

```
POST /meetings/{meetingId}/location/start     — 호스트, location 단계 시작 + 역 계산
GET  /meetings/{meetingId}/midpoint-stations  — 그룹 멤버, 역 후보 조회
```
