# Application Design — 추천 응답 장소상세 확장 (FC-8)

- **작성일**: 2026-07-08
- **근거 요구사항**: requirements-fc9-manual-pick-close-and-reco-detail.md (R2)
- **원칙**: 추가 DB 조회 0건, 스키마 변경 없음, 기존 `PlaceDetailResponse` 재사용.

## 변경 대상 (단일)

### `RecommendationItemResponse` (meeting.presentation.dto)
- 변경 전: `record RecommendationItemResponse(int rank, PlaceSummary place, double score, Long nearestStationId)`
- 변경 후: `record RecommendationItemResponse(int rank, PlaceDetailResponse place, double score, Long nearestStationId)`
- `place` 타입만 `PlaceSummary` → `PlaceDetailResponse` 교체. rank/score/nearestStationId 불변.

### `PlaceSelectionService.getRecommendations`
- 매핑 한 줄 교체: `PlaceSummary.from(placeById.get(r.getPlaceId()))` → `PlaceDetailResponse.from(placeById.get(r.getPlaceId()))`.
- `placeRepository.findByIds(...)`로 이미 `Place` 전체 로딩됨 → **추가 조회 없음**.
- null 처리: 존재하지 않는 placeId면 `PlaceDetailResponse.from`은 NPE 위험. 현행 `PlaceSummary.from`은 null 방어(place==null→null) 존재. → 안전하게 **미존재 추천 항목은 필터링**하거나 `PlaceDetailResponse.from` null 가드 추가 중 택1 (설계: from에 null 가드 추가 권장, `/api/v1/places`는 findByIds 필터로 회피하나 여기선 추천 저장 placeId라 정상적으로 존재).

## 영향 없음
- `PlaceSummary`는 담기/목록 등 다른 응답에서 계속 사용 → **삭제하지 않음**.
- DB·마이그레이션·리포지토리·도메인 변경 없음.
- 인가·상태 흐름 변경 없음.

## R1 (담기 수동 종료)
- **코드 변경 없음.** 기존 `POST /{meetingId}/place-vote`(`startVoting`)가 RECOMMENDED→VOTING 전이 + 백필 + 투표시작을 이미 수행. 프론트 연결만.

## 테스트
- 기존 `getRecommendations` 관련 테스트가 `PlaceSummary` 필드를 검증하면 `PlaceDetailResponse` 필드로 갱신.
- 응답에 roadAddress/businessHours/holiday/naverUrl/vibe/rating 노출 확인.
