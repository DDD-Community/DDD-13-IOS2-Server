# 처리 흐름 — FC-13 자동 확정

```mermaid
flowchart TD
    T{전원투표 or 마감 or 호스트수동} --> R1[1순위 득표 최다]
    R1 -->|동점| R2[2순위 이동시간 합 최소]
    R2 -->|동점| R3[3순위 환승 합 최소]
    R3 -->|동점| R4[4순위 최초 담은시각 빠름]
    R1 --> RANK[1~3위 산출 후보 3미만시 후보수만큼]
    R2 --> RANK
    R3 --> RANK
    R4 --> RANK
    RANK --> SAVE[1위 확정]
    SAVE --> ST[locationStatus CONFIRMED + 장소 고정저장]
```

## 단계
1. 트리거(전원/마감/호스트수동) ⭐
2. meeting_place_vote 집계 + meeting_travel_burden 합산 + 후보별 min picked_at ⭐
3. 공통 비교자로 1위 결정 + 1~3위 산출(전원기권 → 최초 담은시각) ⭐
4. meeting_confirmed_place 저장 + CONFIRMED
5. 결과 조회 시 동일 비교자로 candidates rank 부여 ⭐

## 상태 전이
- VOTING → CONFIRMED
