# Unit of Work Dependency — 중간지점 역 후보 추출 (MVP2)

## 의존 매트릭스

| | Unit 1 | Unit 2 | Unit 3 |
|---|---|---|---|
| Unit 1 | — | 독립 | 선행 필요 |
| Unit 2 | 독립 | — | 선행 필요 |
| Unit 3 | Unit 1 필요 | Unit 2 필요 | — |

## 실행 순서
```
Unit 1 ──┐
          ├──> Unit 3 ──> Build and Test
Unit 2 ──┘
```
Unit 1, 2는 병렬 가능. Unit 3은 두 Unit 완료 후 시작.
(단, 단일 개발자이므로 순서대로: 1 → 2 → 3)

## 공유 리소스
- `DeparturePlaceRepository` (member.domain): Unit 1에서 읽기 전용 사용
- `GroupMemberRepository` (group.domain): Unit 1, Unit 3에서 읽기 전용 사용
- `MeetingRepository` (meeting.domain): Unit 3에서 읽기+쓰기
