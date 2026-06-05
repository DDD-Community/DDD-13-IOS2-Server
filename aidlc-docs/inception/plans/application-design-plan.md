# Application Design Plan — 중간지점 역 후보 추출 (MVP2)

## 설계 결정사항 (질문 없음 — 기존 DDD 패턴 + 요구사항 충분)

| 결정 | 내용 |
|---|---|
| SubwayStation 위치 | subway 신규 바운디드 컨텍스트 |
| MeetingParticipant 위치 | meeting 컨텍스트 (per-meeting 데이터) |
| MidpointStationCandidate 위치 | meeting 컨텍스트 (per-meeting 결과) |
| LocationService | 신규 application service (SRP, MeetingService 분리) |
| PostGIS 쿼리 방식 | native SQL @Query (JPQL 미지원 함수), projection interface 사용 |
| 도메인 모델 좌표 표현 | double lat/lng (기존 Coordinate 패턴 일치), JpaEntity에서 PostGIS Point 변환 |
| StationCandidate | subway domain value object (쿼리 결과 projection) |

## 체크박스 플랜

- [x] components.md 생성
- [x] component-methods.md 생성
- [x] services.md 생성
- [x] component-dependency.md 생성
- [x] application-design.md (통합) 생성
