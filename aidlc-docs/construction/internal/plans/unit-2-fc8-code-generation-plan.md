# U2 추천(FC-8) — Code Generation Plan

> 근거: review/fc8(전체), review/fc4(erd 갱신본), unit-2-fc8 functional-design
> 워크스페이스 루트: `/Users/ym/dev/DDD/Server`

## Step 1. Business Logic Generation
- [x] 1.1 신규 `com.bangawo.place.domain.Place` — 읽기전용 도메인(id, placeId, name, categoryLabel, address, lat/lng, vibe, occasion, reservable, hasParking, rating) + `matchesOccasion`/`matchesCategory`/`vibeOverlap`
- [x] 1.2 신규 `RecommendationCandidate`(record: place, nearestStationId), `ScoredCandidate`(record: candidate, score)
- [x] 1.3 신규 `PlaceScorer`(domain service, 순수함수) — 0.5occ+0.25cat+0.15vibe+0.1rating, rating min-max(NULL=0.5, 단일/동일=1.0)
- [x] 1.4 신규 `PlaceOption` — `categories()` = CategoryLabel 11종
- [x] 1.5 신규 `PlaceRepository`(domain port) — `findCandidates(stationIds, radiusMeters, reservable, parking)`, `findDistinctVibes()`, `findByIds(ids)`
- [x] 1.6 신규 `com.bangawo.meeting.domain.MeetingPlaceRecommendation` + `MeetingPlaceRecommendationRepository`(port) — saveAll/findByMeetingIdOrderByRank
- [x] 1.7 `subway`: `StationCandidate`에 `stationId` 필드 추가

## Step 2. Business Logic Unit Testing
- [x] 2.1 `PlaceScorerTest` — occasion/category/vibe/rating 가중합, rating 결측·동일값 케이스, 빈 categories/vibes=0
- [x] 2.2 `PlaceTest` — matchesOccasion/matchesCategory/vibeOverlap 단위테스트

## Step 3. Business Logic Summary

## Step 4. API Layer Generation
- [x] 4.1 신규 `PlaceRecommendRequest`(record: radiusKm) — meeting.presentation.dto
- [x] 4.2 신규 `RecommendationResponse`/`RecommendationItem`(record) — rank/placeId/name/categoryLabel/score/nearestStationId
- [x] 4.3 신규 `PlaceOptionsResponse`(record: categories, vibes)
- [x] 4.4 `LocationService` → `PlaceSelectionService` 대체(클래스명 변경, 기존 3메서드 유지) — `startLocationPhase` 전체 FC-8 로직 구현(반경 사다리 [start,4,6] 루프, 0건시 PLACE_RECOMMENDATION_EMPTY, radiusKm>6 INVALID_INPUT), `getRecommendations` 신규
- [x] 4.5 신규 `PlaceController`(place.presentation) — `GET /api/v1/places/options`
- [x] 4.6 `LocationController` — `PlaceSelectionService` 주입으로 교체, `startLocationPhase`에 `@RequestBody(required=false) PlaceRecommendRequest` 추가, `GET /{meetingId}/recommendations` 추가

## Step 5. API Layer Unit Testing
- [x] 5.1 `PlaceSelectionServiceTest` — 가드 통과 후 반경 사다리 호출 순서, 0건시 다음 반경, 6km 소진시 PLACE_RECOMMENDATION_EMPTY, radiusKm>6 INVALID_INPUT, completeRecommendation 호출 검증(mock)

## Step 6. API Layer Summary

## Step 7. Repository Layer Generation
- [x] 7.1 신규 `PlaceJpaEntity`(`@JdbcTypeCode(ARRAY)` vibe/occasion), `PlaceJpaRepository`(네이티브: findCandidateIdsNearStations, findDistinctVibes), `PlaceRepositoryImpl`
- [x] 7.2 신규 `MeetingPlaceRecommendationJpaEntity`, `MeetingPlaceRecommendationJpaRepository`, `MeetingPlaceRecommendationRepositoryImpl` (기존 MidpointStationCandidate 패턴 동일)
- [x] 7.3 `SubwayStationJpaRepository` 네이티브 쿼리 — 반경 사다리 CTE(2k/4k/6k) + `station_id` SELECT 추가 (메서드 시그니처 불변)
- [x] 7.4 `SubwayStationRepositoryImpl` — StationCandidate 매핑에 stationId 추가

## Step 8. Repository Layer Unit Testing
- [x] 8.1 스킵 (네이티브 PostGIS 쿼리는 Build & Test 단계 통합테스트로 커버 — 기존 컨벤션과 동일)

## Step 9. Repository Layer Summary

## Step 10. Database Migration Scripts
- [x] 10.1 ~~V19~~ — **취소**: place.occasion(기존 V12 컬럼)에 이미 실데이터 있음을 데이터 확인 후 발견, 신규 컬럼 불필요 (User 지적으로 정정)
- [x] 10.2 `V20__create_meeting_place_recommendation.sql`

## Step 11. Documentation Generation
- [x] 11.1 `review/fc8/api.md` — 응답 예시 보강(필요시)
- [x] 11.2 `review/fc8/erd.md` — 기존 내용과 실제 컬럼 일치 확인

## Step 12. Deployment Artifacts
- [x] 12.1 해당 없음
