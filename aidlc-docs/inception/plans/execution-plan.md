# Execution Plan — 중간지점 역 후보 추출 (MVP2)

## Detailed Analysis Summary

### Change Impact Assessment
- **User-facing changes**: Yes — 신규 API 2개 (location/start, midpoint-stations)
- **Structural changes**: Yes — subway 신규 바운디드 컨텍스트 도입
- **Data model changes**: Yes — 신규 테이블 3개 (meeting_participant, subway_station, midpoint_station_candidate)
- **API changes**: Yes — 신규 엔드포인트 2개, 기존 CreateMeeting 로직 수정
- **NFR impact**: Low — PostGIS/Cloud SQL 이미 사용 중, GIST 인덱스 추가

### Component Relationships
- **Primary**: subway 컨텍스트 (신규), meeting 컨텍스트 (확장)
- **Dependency**: meeting application layer → SubwayStationRepository (subway context)
- **Modified**: CreateMeetingService — meeting 생성 시 meeting_participant 자동 생성 추가

### Risk Assessment
- **Risk Level**: Medium
- **주요 리스크**: PostGIS native query (JPA 밖), 새 컨텍스트 간 의존 방향
- **Rollback**: Flyway 적용된 마이그레이션은 수정 불가 원칙 → 신중 설계

---

## Workflow Visualization (Text)

```
INCEPTION PHASE
  [x] Workspace Detection       COMPLETED
  [x] Reverse Engineering       COMPLETED (갱신)
  [x] Requirements Analysis     COMPLETED
  [ ] User Stories              SKIP
  [x] Workflow Planning         IN PROGRESS
  [ ] Application Design        EXECUTE
  [ ] Units Generation          EXECUTE

CONSTRUCTION PHASE
  Unit 1: meeting_participant
    [ ] Functional Design       EXECUTE
    [ ] Code Generation         EXECUTE
  Unit 2: subway context
    [ ] Functional Design       EXECUTE
    [ ] Code Generation         EXECUTE
  Unit 3: midpoint 계산 + API
    [ ] Functional Design       EXECUTE
    [ ] Code Generation         EXECUTE
  [ ] Build and Test            EXECUTE

OPERATIONS PHASE
  [ ] Operations                PLACEHOLDER
```

---

## Phase 결정

### INCEPTION PHASE
- [x] Workspace Detection — COMPLETED
- [x] Reverse Engineering — COMPLETED (stale 재실행)
- [x] Requirements Analysis — COMPLETED
- [ ] User Stories — **SKIP** (단일 기능, 단일 역할)
- [x] Workflow Planning — IN PROGRESS
- [ ] Application Design — **EXECUTE** (subway 신규 컨텍스트 컴포넌트 설계)
- [ ] Units Generation — **EXECUTE** (3개 유닛 분해)

### CONSTRUCTION PHASE

**Unit 1 — meeting_participant 도입**
- [ ] Functional Design — EXECUTE
- [ ] NFR Requirements — SKIP
- [ ] NFR Design — SKIP
- [ ] Infrastructure Design — SKIP
- [ ] Code Generation — EXECUTE

**Unit 2 — subway context 신규**
- [ ] Functional Design — EXECUTE (PostGIS native query, 새 컨텍스트 DDD 설계)
- [ ] Code Generation — EXECUTE

**Unit 3 — midpoint 계산 + API**
- [ ] Functional Design — EXECUTE (상태전이, 계산 오케스트레이션, API)
- [ ] Code Generation — EXECUTE

- [ ] Build and Test — EXECUTE

---

## 3-Unit 분해

### Unit 1 — meeting_participant 도입
**범위:**
- V11__create_meeting_participant.sql
- MeetingParticipant 도메인 (meeting context)
- MeetingParticipantRepository 인터페이스 + 구현체
- CreateMeetingService 수정 (meeting 생성 시 group_member → meeting_participant 자동 복사)

**의존성:** 독립

### Unit 2 — subway context 신규
**범위:**
- V12__create_subway_station.sql (DDL만, GIST 인덱스 포함)
- SubwayStation 도메인 (subway context 신규)
- SubwayStationRepository 인터페이스 + 구현체 (native PostGIS 쿼리)

**의존성:** 독립

### Unit 3 — midpoint 계산 + API
**범위:**
- V13__create_midpoint_station_candidate.sql
- MidpointStationCandidate 도메인 (meeting context)
- MidpointStationCandidateRepository 인터페이스 + 구현체
- MidpointCalculationService (meeting_participant + subway_station 조합, PostGIS centroid)
- Meeting 도메인: startLocationPhase() 메서드
- LocationService: 호스트 권한 체크 + 계산 오케스트레이션
- ErrorCode 추가
- POST /meetings/{id}/location/start
- GET /meetings/{id}/midpoint-stations
- Request/Response DTO

**의존성:** Unit 1 완료 + Unit 2 완료 후

---

## 실행 순서
Unit 1 → Unit 2 → Unit 3 → Build and Test
(Unit 1,2 독립이나 Unit 3이 둘 다 필요)

---

## Success Criteria
- compileJava BUILD SUCCESSFUL
- V11/V12/V13 마이그레이션 적용 완료
- POST /meetings/{id}/location/start → midpoint_station_candidate 3개 저장
- GET /meetings/{id}/midpoint-stations → rank 1/2/3 반환
- 비호스트 호출 시 403
