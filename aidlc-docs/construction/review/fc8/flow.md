# FC-8 로직 흐름

## 범위 정의

| 기능 | MVP1 포함 여부 |
|---|---|
| 모임 자동 종료 (스케줄러) | ✅ FC-7에서 구현 완료 |
| 새 모임 시작 | ✅ FC-7에서 구현 완료 |
| 그룹 종료 (호스트 수동) | ✅ FC-8 구현 |
| 호스트 위임 | ❌ MVP2 이후 |
| 그룹 탈퇴 | ❌ MVP2 이후 |

---

## 1. 그룹 종료

```
PATCH /api/v1/groups/{groupId}/close

[Controller] → [GroupService.closeGroup]
  1. JWT에서 memberId 추출
  2. group 조회 (없으면 404)
  3. groupMember 조회 → HOST인지 확인 (아니면 403)
  4. group.isActive() 확인 (이미 CLOSED면 400)
  5. group.close()  ← status = CLOSED
  6. group 저장
  응답: 204 No Content
```

---

## 상태 전이 요약

```
GroupStatus:

ACTIVE : 그룹 생성 시 기본값.
CLOSED : 호스트가 수동으로 종료하거나 (FC-8),
         그룹 내 모든 모임이 종료된 이후 재사용 없을 때 (미래 고려).

ACTIVE ──[호스트 수동 종료]──────────────────────────→ CLOSED
```
