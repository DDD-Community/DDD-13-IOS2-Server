# 비즈니스 규칙 — FC-8 중간지역 산출 + 장소 추천

## 권한/전제
- **호스트만** 시작. `dateVoteStatus == COMPLETED`(날짜 확정됨) AND `locationStatus == BEFORE` 일 때만.
  - 날짜 미확정 → PLACE_PHASE_NOT_READY(400)
  - 이미 시작 → LOCATION_PHASE_ALREADY_STARTED(400)
- 참여(ATTEND/LATE) 구성원 **전원 출발지 보유** 필요. 없으면 PARTICIPANT_DEPARTURE_NOT_SET(400)

## 상태 모델 (신규 — 4-state로 교체)
- locationStatus: **BEFORE → RECOMMENDED → VOTING → CONFIRMED**
- FC-8 완료 시 BEFORE → **RECOMMENDED**

## 중간역 산출
- 참여자 좌표 중심점(centroid) 기준 근접 역 최대 3개(rank 1~3)
- 반경 사다리: **2km → (0개면) 4km → 6km**. 6km도 0개면 MIDPOINT_STATION_NOT_FOUND(400)

## 장소 후보 — HARD 필터
- 3개 역 반경 N km(시작 2km, 부족 시 4·6) 내 place
- 예약/주차: **모임 생성 시 입력한 `meeting.reservable`/`meeting.parking` 값을 그대로 사용** (location/start 요청에서 다시 입력받지 않음). NULL(조건 없음) **관대**: TRUE·NULL 포함, FALSE만 제외
- 6km까지 0개 → PLACE_RECOMMENDATION_EMPTY(400)

## 스코어링 — SOFT (거리 제외)
- `score = 0.5·occasion + 0.25·category + 0.15·vibe + 0.1·rating`
  - occasion = place.occasion(기존 AI 태그)가 모임 themeTagCode의 **theme_tag.display_name**(예: DINING→"회식") 포함 ? 1 : 0 — 신규 컬럼 없이 기존 occasion 데이터 그대로 사용
  - category = place.category_label ∈ 모임 categoryLabels ? 1 : 0
  - vibe = |모임 vibes ∩ place.vibe| / |모임 vibes|  (분모 0이면 0)
  - rating = 후보집합 min-max 정규화(결측 0.5)
- 내림차순 **상위 15** (미만이면 가능한 만큼, 역별 최소보장 없음)

## 역 귀속
- 각 후보를 3개 역 중 **최근접역**에 라벨(`meeting_place_recommendation.nearest_station_id`) → FC-9 역 탭 필터 기준
- 3개 역 탭 소스인 `midpoint_station_candidate`는 **동일한 `station_id`**(중간역 쿼리의 `MIN(station_id)`, 장소 검색에 넘긴 stationIds)를 저장 → 탭(`station_id`) ↔ 장소(`nearest_station_id`) 정확히 조인됨
- `latitude`/`longitude`도 함께 저장 → 프론트 지도 핀 표시 가능 (V27)

## 결과
- meeting_place_recommendation 15건 스냅샷 저장, locationStatus → RECOMMENDED

## 장소 상세 조회 (`GET /api/v1/places?ids=`) — [V32 보강]
- read-only 범용 조회. 권한/상태 가드 없음(인증만). 비즈니스 로직 없음 — place 마스터 데이터 그대로 노출
- 응답에 영업시간(`businessHours`)·휴무(`holiday`)·도로명(`roadAddress`)·지번(`address`)·네이버링크(`naverUrl`) 포함
- `address` 의미 = **지번주소** (V32에서 도로명→지번 변경). 도로명은 `roadAddress`
- `businessHours`/`holiday`는 **표시용 원문 텍스트** — 서버는 "영업중 여부" 등 판단 안 함
- 값 미적재 시 `null` (에러 아님). 미존재 placeId는 결과에서 제외, 요청 순서 보존
- place 데이터는 read-only(외부 파이프라인 적재) — 애플리케이션 쓰기 경로 없음
- 스코프 제외: 거리(좌표 비의존 유지), 함께담기 N(모임 맥락)
