# Unit of Work Plan — 중간지점 역 후보 추출 (MVP2)

## 분해 방식
DDD 바운디드 컨텍스트 + 의존 관계 기준 3개 유닛. 순차 실행 (Unit 3이 1+2 의존).

## 체크박스 플랜
- [x] unit-of-work.md 생성
- [x] unit-of-work-dependency.md 생성
- [x] unit-of-work-story-map.md 생성

## 유닛 요약
| 유닛 | 이름 | 핵심 산출물 | 의존 |
|---|---|---|---|
| Unit 1 | meeting_participant 도입 | V11 migration, MeetingParticipant 도메인+인프라, CreateMeetingService 수정 | 없음 |
| Unit 2 | subway context 신규 | V12 migration, SubwayStation 도메인+인프라, native PostGIS 쿼리 | 없음 |
| Unit 3 | midpoint 계산 + API | V13 migration, MidpointStationCandidate, LocationService, API 2개 | Unit 1 + Unit 2 |
