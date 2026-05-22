# Unit Test Execution — FC-6 모임 리스트

## 테스트 파일 목록

| 파일 | 테스트 수 | 검증 범위 |
|---|---|---|
| `MeetingComputeListStatusTest` | 7 | `Meeting.computeListStatus()` 도메인 로직 |
| `MeetingListServiceTest` | 4 | `MeetingListService.getMyMeetingList()` 애플리케이션 로직 |

## 실행 방법

### FC-6 관련 테스트만 실행

```bash
./gradlew test --tests "com.bangawo.meeting.*"
```

### 전체 유닛 테스트 실행

```bash
./gradlew test
```

### 특정 테스트 클래스 실행

```bash
./gradlew test --tests "com.bangawo.meeting.domain.MeetingComputeListStatusTest"
./gradlew test --tests "com.bangawo.meeting.application.MeetingListServiceTest"
```

## 성공 기준

- Total 17개, Failures 0, Errors 0
- 리포트 위치: `build/reports/tests/test/index.html`

## MeetingComputeListStatusTest 커버리지

| 케이스 | 조건 | 기대 결과 |
|---|---|---|
| CLOSED | confirmedDate < 오늘 | CLOSED |
| CLOSED 아님 | confirmedDate = 오늘 | CONFIRMED |
| CLOSED 아님 | confirmedDate null + 날짜 지남 | IN_PROGRESS |
| IN_PROGRESS | locationStatus = IN_PROGRESS | IN_PROGRESS |
| IN_PROGRESS | dateVoteStatus = IN_PROGRESS | IN_PROGRESS |
| CONFIRMED | 둘 다 COMPLETED | CONFIRMED |
| IN_PROGRESS | 둘 다 BEFORE (초기) | IN_PROGRESS |

## MeetingListServiceTest 커버리지

| 케이스 | 기대 결과 |
|---|---|
| 소속 그룹 없음 | 빈 리스트 반환 |
| 3개 그룹 (각기 다른 status) | IN_PROGRESS → CONFIRMED → CLOSED 정렬 |
| 탈퇴 회원 포함 | nickname/profileImageUrl null |
| 2명 구성원 (joinedAt 순서 역전) | 선합류자 먼저 정렬 |
