# Unit Test Instructions

## Test Framework
- **JUnit 5** + **Mockito** (@Mock / @InjectMocks / @ExtendWith(MockitoExtension.class))
- **AssertJ** for assertions

## Test Scope (77 tests, 0 failures)

### Meeting Context
| Test Class | Coverage |
|---|---|
| `PlacePickServiceTest` | 담기/취소/현황/투표시작/자동VOTING전환 |
| `PlaceVoteServiceTest` | 투표제출/재투표/최대투표수 검증/전원투표시 자동확정 |
| `PlaceConfirmServiceTest` | 4단계 순위 로직 (득표↓·이동시간합↑·환승합↑·담긴순↑) |
| `PlaceVoteSchedulerServiceTest` | 마감기한 초과 세션 자동 CLOSED + 확정 처리 |

### Subway Context
| Test Class | Coverage |
|---|---|
| `SubwayGraphTest` | Dijkstra 경로 계산, 환승 가중치, 도달불가 station |

## Run Command
```bash
./gradlew test --no-daemon
# 특정 클래스만
./gradlew test --tests "com.bangawo.meeting.application.PlaceVoteServiceTest" --no-daemon
```

## Key Patterns
- `@ExtendWith(MockitoExtension.class)` — Spring context 불필요
- `@InjectMocks` 대상에 새 의존성 추가 시 반드시 `@Mock` 선언 추가
- Business rule 검증: `assertThatThrownBy(...).isInstanceOf(BusinessException.class)`
