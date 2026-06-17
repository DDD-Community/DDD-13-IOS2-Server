# U2 추천(FC-8) — Business Logic Model

## 오케스트레이션 (`PlaceSelectionService.startLocationPhase`, 기존 `LocationService` 대체)
1. 호스트/가드 검증 (기존 LocationService 로직 재사용)
2. 참여자 출발지 검증 (기존 로직 재사용)
3. `MidpointCalculationService.calculate` → 3개 역(반경 사다리는 SQL 내부로 이동)
4. `MidpointStationCandidate` 저장 (기존 동작 유지)
5. 반경 사다리 루프 [start,4,6] → `PlaceRepository.findCandidates(stationIds, radiusMeters, reservable, parking)` → 0건이면 다음 반경
6. `PlaceScorer.score(...)` → 상위 15
7. `MeetingPlaceRecommendation` 일괄 저장 + `meeting.completeRecommendation()` (같은 트랜잭션)

## 변경/신규 파일 매핑
- 신규: `place` 패키지 전체(domain/infrastructure), `MeetingPlaceRecommendation`(+infra), V19, V20
- 수정: `SubwayStationJpaRepository`(SQL 반경사다리), `StationCandidate`(stationId 필드), `LocationController`(요청 body 확장 + `/recommendations` 추가)
- 대체: `LocationService` → `PlaceSelectionService`(클래스명 변경, 기존 메서드 3종 포함 + FC-8 로직 추가)
- 신규: `PlaceController`(`GET /api/v1/places/options`)

## 영향 받는 기존 테스트
- 없음 (LocationService에 대한 기존 단위테스트 없었음 — 확인됨)
