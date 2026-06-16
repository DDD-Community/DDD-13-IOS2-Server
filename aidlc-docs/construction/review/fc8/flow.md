# 처리 흐름 — FC-8 추천

```mermaid
sequenceDiagram
    Host->>API: POST location/start radiusKm reservable parking
    API->>Meeting: 가드 dateVoteStatus COMPLETED and locationStatus BEFORE
    API->>Participant: ATTEND LATE 출발지 검증
    API->>PostGIS: centroid 중간역 3개 반경 2 4 6 사다리
    API->>PlaceRepo: findCandidates 역3 반경 하드필터
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
