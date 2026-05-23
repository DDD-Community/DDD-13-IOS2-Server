# Domain Entities — FC-7-1 내 정보 수정

## 변경 대상 도메인

### GroupMember (group 컨텍스트) — 메서드 추가
```
GroupMember
  + updateAttendance(AttendanceStatus newStatus): void
```
- 비즈니스 로직을 도메인에 위치 (Service에서 직접 필드 변경 금지)

### DeparturePlace (member 컨텍스트) — 메서드 추가
```
DeparturePlace
  + update(String label, String address, Coordinate coordinate): void
```
- label, address, coordinate 변경 + updatedAt 갱신
- isDefault는 변경하지 않음 (Q4=B)

## 수정 없는 기존 도메인

| 도메인 | 이유 |
|---|---|
| Group | 조회 없음 |
| Meeting | 조회 없음 |
| Member | 조회 없음 |

## 신규 DTO

| DTO | 위치 | 용도 |
|---|---|---|
| `AttendanceUpdateRequest` | group/presentation/dto | 참석여부 수정 요청 |
| `DeparturePlaceRequest` | member/presentation/dto | 출발지 추가/수정 요청 (공용) |

## 신규 Application Service

| 클래스 | 위치 | 역할 |
|---|---|---|
| `GroupMemberService` | group/application | 참석여부 수정 오케스트레이션 |

## 신규 Controller

| 클래스 | 경로 | 메서드 |
|---|---|---|
| `GroupMemberController` | group/presentation | PATCH /api/v1/groups/{groupId}/members/me/attendance |
