# U1 — Business Logic Model

## 변경 흐름
1. `LocationStatus` enum 3-state → 4-state 교체
2. `Meeting` 도메인에 가드/전환 메서드 4개 추가, 기존 `startLocationPhase()` 제거 후 `assertCanStartLocationPhase()`로 대체
3. `LocationService.startLocationPhase()` 내부 호출부를 `meeting.assertCanStartLocationPhase()`로 교체 (RECOMMENDED 전환은 U2에서 `completeRecommendation()` 호출 추가 예정 — U1 시점엔 호출 없음, 그대로면 BEFORE에 머무름)
4. `Meeting.computeListStatus` 조건식 갱신
5. `CreateGroupRequest`/`CreateMeetingRequest`에 필드 추가 → `GroupController` → `GroupService` → `Meeting.create` 까지 파라미터 전달 체인 확장
6. `ErrorCode`에 8개 신규 코드 append
7. V18 Flyway 작성

## 영향 받는 기존 테스트
- `MeetingComputeListStatusTest`: LocationStatus.IN_PROGRESS/COMPLETED 참조 부분을 RECOMMENDED/VOTING/CONFIRMED로 갱신
