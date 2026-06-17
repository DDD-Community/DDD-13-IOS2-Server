# U2 추천(FC-8) — Business Rules

## 시작 가드 (`PlaceSelectionService.startLocationPhase`)
1. 호스트만 (`NOT_GROUP_HOST`)
2. `meeting.assertCanStartLocationPhase()` — 날짜 미확정(`PLACE_PHASE_NOT_READY`) / 이미 시작(`LOCATION_PHASE_ALREADY_STARTED`)
3. JOIN/LATE 참여자 전원 출발지 보유 (`PARTICIPANT_DEPARTURE_NOT_SET`)

## 중간역 산출 (기존 컴포넌트 재사용, SQL 내부 반경 사다리)
- centroid 기준 2km → (0개) 4km → (0개) 6km, 6km도 0개면 `MIDPOINT_STATION_NOT_FOUND`
- 최대 3개(rank 1~3), 기존 `midpoint_station_candidate` 저장 로직 그대로 재사용

## 장소 후보 — HARD 필터 + 반경 사다리 (신규)
- 요청 `radiusKm`(선택, 기본 2, 범위 (0,6]) 기준 사다리: `[radiusKm, 4, 6]`에서 radiusKm 이상인 값만 오름차순 적용 (예: 기본2→[2,4,6], 4→[4,6], 6→[6])
- radiusKm > 6 → `INVALID_INPUT`(400)
- 각 반경에서 3개 역 중심 PostGIS 반경 검색, 0건이면 다음 반경, 6km까지 0건이면 `PLACE_RECOMMENDATION_EMPTY`
- reservable/parking: **요청 바디에 없음.** 모임 생성 시 저장된 `meeting.reservable`/`meeting.parking` 값을 그대로 사용. NULL 관대(TRUE·NULL 포함, FALSE만 제외)

## SOFT 스코어링
- `score = 0.5*occasion + 0.25*category + 0.15*vibe + 0.1*rating`
- occasion: `place.occasion`(기존 컬럼)이 모임 `themeTagCode`의 `theme_tag.display_name` 포함 시 1, 아니면 0 — 신규 컬럼 없음
- category: 모임 `categoryLabels`가 비어있으면 0, 아니면 `categoryLabels`에 `place.categoryLabel` 포함 시 1
- vibe: 모임 `vibes`가 비어있으면 0, 아니면 교집합/모임vibes개수
- rating: 후보집합 min-max 정규화, NULL은 0.5, 전부 동일/단일 후보는 1.0
- 상위 15개 (15 미만이면 가능한 만큼, 역별 최소보장 없음)

## 역 귀속
- 각 후보를 3개 역 중 최근접역에 라벨 (PostGIS 거리 비교, PlaceRepository 쿼리 내부에서 결정)

## 완료
- `meeting_place_recommendation` 15건(이하) 스냅샷 저장 + `meeting.completeRecommendation()` (BEFORE→RECOMMENDED) — 동일 트랜잭션

## 조회 (GET)
- `/recommendations`: rank/placeId/name/categoryLabel/score/nearestStationId, rank 오름차순
- `/places/options`: categories=CategoryLabel 고정 11종, vibes=`place.vibe` distinct
