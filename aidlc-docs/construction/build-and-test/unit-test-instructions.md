# Unit Test Instructions

> 최신 갱신: 2026-06-24 — FC-12/13 보완 반영

## Test Framework
- **JUnit 5** + **Mockito** (`@Mock` / `@InjectMocks` / `@ExtendWith(MockitoExtension.class)`)
- **AssertJ** for assertions
- 협력자 많은 서비스는 `@MockitoSettings(strictness = LENIENT)` 사용

## Test Scope (94 tests, 0 failures)

### Meeting Context — 이번 사이클 핵심
| Test Class | Coverage |
|---|---|
| `PlaceVoteServiceTest` | startVoting(담기 0개여도 시작) / **백필 0·1·2·≥3개** / **placeId 후보검증** / 다중제한 / 정렬·**memberStatuses 전원공개** / **getPlaceTravelBurden(거리보기)** |
| `PlaceConfirmServiceTest` | **동점 4단계(min pickedAt)** / 득표 1위 / **1~3위 rank** / **confirmByHost**(호스트·VOTING 분기) |
| `MeetingPlacePickTest` | `of`(USER) / **`ofSystem`(SYSTEM, memberId null)** |
| `PlacePickServiceTest` | 담기/취소/현황/투표시작 (기존) |

### 기타 (기존 유지)
- `MeetingComputeListStatusTest`, `MeetingCreateTest`, `MeetingLocationPhaseTest`, `MeetingPickDeadlineTest`, `MeetingListServiceTest`, `PlaceSelectionServiceTest` 등

## Run Command
```bash
./gradlew test
# 특정 클래스만
./gradlew test --tests "com.bangawo.meeting.application.PlaceVoteServiceTest"
./gradlew test --tests "com.bangawo.meeting.application.PlaceConfirmServiceTest"
```

## Key Patterns
- `@ExtendWith(MockitoExtension.class)` — Spring context 불필요
- `@InjectMocks` 대상에 새 의존성(MemberRepository, GroupMemberRepository 등) 추가 시 반드시 `@Mock` 선언 추가
- 백필 검증: `ArgumentCaptor<List<MeetingPlacePick>>` 로 `saveAll` 인자 캡처
- 비즈니스 규칙 위반: `assertThatThrownBy(...).hasFieldOrPropertyWithValue("errorCode", ErrorCode.XXX)`
