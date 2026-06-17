# U1 기반 — Code Generation Plan

> 근거: review/fc8(erd.md, rules.md), review/fc4(rules.md, api.md) + unit-1-fc8 functional-design
> 워크스페이스 루트: `/Users/ym/dev/DDD/Server` (Brownfield — 기존 파일 수정 우선)

## Step 1. Business Logic Generation
- [x] 1.1 `LocationStatus.java` — `BEFORE, IN_PROGRESS, COMPLETED` → `BEFORE, RECOMMENDED, VOTING, CONFIRMED`
- [x] 1.2 신규 `CategoryLabel.java` (enum, **com.bangawo.global.common** — U2의 PlaceOption과 중복 방지 위해 공유 위치로 이동) — 한식/중식/일식/양식/카페/디저트/주점/분식/아시아음식/뷔페/기타
- [x] 1.3 `Meeting.java`
  - `categoryLabels: List<String>`, `vibes: List<String>` 필드 추가
  - `create(groupId, name, themeTagCode, categoryLabels, vibes)` 시그니처 확장 + categoryLabels는 CategoryLabel 검증(INVALID_INPUT)
  - `startLocationPhase()` 제거 → `assertCanStartLocationPhase()`(가드만, 상태변경 없음) 신설
  - `completeRecommendation()`, `toVoting()`, `toConfirmed()` 신설
  - `computeListStatus()`: `locationStatus == CONFIRMED` 조건으로 갱신
- [x] 1.4 `ErrorCode.java` — 8건 append: `PLACE_PHASE_NOT_READY`, `PLACE_RECOMMENDATION_EMPTY`, `LOCATION_NOT_RECOMMENDED`, `PLACE_PICK_CLOSED`, `PLACE_VOTE_DEADLINE_INVALID`, `PLACE_VOTE_NOT_IN_PROGRESS`, `PLACE_VOTE_LIMIT_EXCEEDED`, `PLACE_NOT_CONFIRMED`

## Step 2. Business Logic Unit Testing
- [x] 2.1 `MeetingComputeListStatusTest.java` — LocationStatus.IN_PROGRESS/COMPLETED 참조 → RECOMMENDED/VOTING/CONFIRMED로 교체
- [x] 2.2 신규 `MeetingLocationPhaseTest.java` — assertCanStartLocationPhase/completeRecommendation/toVoting/toConfirmed 가드·전이·예외 케이스
- [x] 2.3 신규 `MeetingCreateTest.java` — categoryLabels 유효값/잘못된 값(INVALID_INPUT), vibes 자유값(검증 없음) 통과 확인

## Step 3. Business Logic Summary
- [x] 3.1 변경 요약 기록 (완료 메시지에 포함, 별도 파일 없음)

## Step 4. API Layer Generation
- [x] 4.1 `CreateGroupRequest.java` — `categoryLabels: List<String>`(선택), `vibes: List<String>`(선택) 추가
- [x] 4.2 `CreateMeetingRequest.java` (record) — 동일 필드 추가
- [x] 4.3 `GroupService.createGroupWithMeeting(...)` / `createNextMeeting(...)` — 파라미터 확장, `Meeting.create(...)`에 전달
- [x] 4.4 `GroupController.createGroup` / `createNextMeeting` — request의 새 필드를 서비스 호출에 전달

## Step 5. API Layer Unit Testing
- [x] 5.1 신규 `GroupServiceTest.java` — createGroupWithMeeting/createNextMeeting이 categoryLabels/vibes를 Meeting.create에 전달하는지 mock 검증

## Step 6. API Layer Summary
- [x] 6.1 변경 요약 기록

## Step 7. Repository Layer Generation
- [x] 7.1 `MeetingJpaEntity.java` — `categoryLabels`, `vibes` 컬럼 매핑 (`@JdbcTypeCode(SqlTypes.ARRAY)`, `columnDefinition = "text[]"`), `from()`/`toDomain()` 갱신

## Step 8. Repository Layer Unit Testing
- [x] 8.1 (기존 Repository 레벨 단위테스트 없음 — Build & Test 단계의 통합테스트로 커버, 스킵)

## Step 9. Repository Layer Summary
- [x] 9.1 변경 요약 기록

## Step 10. Database Migration Scripts
- [x] 10.1 `V18__add_meeting_category_labels_vibes.sql` — `meeting.category_labels TEXT[]`, `meeting.vibes TEXT[]` 추가 + 컬럼 코멘트 갱신
- [x] 10.2 (V26 데이터 마이그레이션은 생성하지 않음 — U5 이후 보류, 결정사항만 유지)

## Step 11. Documentation Generation
- [x] 11.1 `review/fc4/api.md` — categoryLabels/vibes 필드 설명이 실제 구현(검증 방식 포함)과 일치하는지 확인, 필요시 보강
- [x] 11.2 `review/fc4/flow.md` — 생성 안 함 (User 결정: 구버전 기능, 생략)

## Step 12. Deployment Artifacts
- [x] 12.1 해당 없음 (인프라/배포 변경 없음)

## 영향 받는 기존 호출부 (수정만, 신규 아님)
- `LocationService.startLocationPhase()` 내부의 `meeting.startLocationPhase()` 호출 → `meeting.assertCanStartLocationPhase()`로 교체 (이 메서드의 추천 로직 본체는 U2에서 확장)
