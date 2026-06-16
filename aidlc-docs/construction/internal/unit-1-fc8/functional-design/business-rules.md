# U1 — Business Rules

## 상태 전이
- BEFORE → RECOMMENDED (U2, completeRecommendation)
- RECOMMENDED → VOTING (U3/U4, toVoting)
- VOTING → CONFIRMED (U5, toConfirmed)
- 역행 전이 없음 (재시작/리셋 기능 범위 외)

## 가드
- `assertCanStartLocationPhase`: dateVoteStatus != COMPLETED → PLACE_PHASE_NOT_READY / locationStatus != BEFORE → LOCATION_PHASE_ALREADY_STARTED
- `toVoting`: locationStatus != RECOMMENDED → LOCATION_NOT_RECOMMENDED
- `toConfirmed`: locationStatus != VOTING → PLACE_VOTE_NOT_IN_PROGRESS

## 그룹/모임 생성 확장
- categoryLabels/vibes는 선택 입력, 그룹 테이블엔 저장 안 함, meeting에만 저장
- 다음 모임 생성(`createNextMeeting`)도 동일하게 입력 가능

## 데이터 마이그레이션 (V26, 차후 생성)
- IN_PROGRESS → RECOMMENDED, COMPLETED → CONFIRMED, BEFORE는 유지
