# Unit Test Execution

## 테스트 파일 목록

| 파일 | 테스트 수 | 검증 범위 | FC |
|---|---|---|---|
| `GlobalExceptionHandlerTest` | 2 | BusinessException → HTTP 응답 변환 | 공통 |
| `JwtProviderTest` | 4 | JWT 발급 · 검증 · 만료 | 공통 |
| `MeetingComputeListStatusTest` | 7 | `Meeting.computeListStatus()` 도메인 로직 | FC-6 |
| `MeetingListServiceTest` | 4 | `MeetingListService.getMyMeetingList()` 애플리케이션 로직 | FC-6 |
| `GroupMemberServiceTest` | 2 | 참석여부 수정 · 미구성원 예외 | FC-7-1 |
| `DeparturePlaceServiceTest` | 5 | 출발지 추가(방어로직) · 수정 · 한도초과 예외 | FC-7-1 |

## 실행 방법

### 전체 유닛 테스트 실행

```bash
./gradlew test
```

### FC-7-1 관련 테스트만 실행

```bash
./gradlew test --tests "com.bangawo.group.application.GroupMemberServiceTest"
./gradlew test --tests "com.bangawo.member.application.DeparturePlaceServiceTest"
```

## 성공 기준

- Total 24개, Failures 0, Errors 0
- 리포트 위치: `build/reports/tests/test/index.html`

## GroupMemberServiceTest 커버리지

| 케이스 | 기대 결과 |
|---|---|
| 정상 요청 | attendanceStatus 변경 후 save 호출 |
| 미구성원 | `403 GROUP_003` BusinessException |

## DeparturePlaceServiceTest 커버리지

| 케이스 | 기대 결과 |
|---|---|
| 첫 등록 (isDefault=false 전달) | isDefault 강제 true로 저장 |
| 최대 3개 초과 | `400 MEMBER_003` BusinessException |
| isDefault=true 요청 | clearDefaultByMemberId 호출 |
| 수정 성공 | label/address/좌표 변경, isDefault 유지 |
| 타인 소유 수정 시도 | `404 MEMBER_005` BusinessException |
