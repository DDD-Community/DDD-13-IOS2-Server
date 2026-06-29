# 처리 흐름 — FC-8 추천

```mermaid
sequenceDiagram
    Host->>API: POST location/start radiusKm
    API->>Meeting: 가드 dateVoteStatus COMPLETED and locationStatus BEFORE
    API->>Participant: ATTEND LATE 출발지 검증
    API->>PostGIS: centroid 중간역 3개 반경 2 4 6 사다리
    API->>Meeting: reservable parking 조회(생성시 저장된 값)
    API->>PlaceRepo: findCandidates 역3 반경 reservable parking 하드필터
    PlaceRepo-->>API: 후보 거리 최근접역 포함
    API->>Scorer: soft 점수 정규화
    Scorer-->>API: 상위 15 귀속역
    API->>DB: meeting_place_recommendation 저장
    API->>Meeting: locationStatus RECOMMENDED
    API-->>Host: 추천 결과
```

## 단계
1. 가드 통과 → 2. 참여자 출발지 검증 → 3. 중간역3(2→4→6km)
4. 후보 수집(HARD: 반경+예약/주차) → 5. SOFT 점수 → 6. top15+귀속역 → 7. 스냅샷 + RECOMMENDED

## 상태 전이
- BEFORE → RECOMMENDED

## 엣지
- 역 0개 → MIDPOINT_STATION_NOT_FOUND
- 장소 0개(6km) → PLACE_RECOMMENDATION_EMPTY
- 15 미만 → 가능한 만큼

## 장소 상세 조회 흐름 (`GET /api/v1/places?ids=`) — [V32 보강]
```mermaid
sequenceDiagram
    Client->>API: GET /places?ids=12,7,99
    API->>PlaceRepo: findByIds([12,7,99])
    PlaceRepo-->>API: Place[] (미존재 99 제외)
    API->>API: 요청 순서대로 정렬 + PlaceDetailResponse.from 매핑
    API-->>Client: [{...address(지번),roadAddress,businessHours,holiday,naverUrl...}]
```
- 단계: ids 수신 → findByIds 조회 → 요청 순서 보존 정렬(미존재 제외) → DTO 매핑(상세필드 포함) → 반환
- 상태 전이: 없음 (read-only, 모임 상태 무관)
- 엣지: 전부 미존재 → 빈 배열 / 미적재 필드 → null
