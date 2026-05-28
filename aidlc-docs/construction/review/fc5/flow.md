# FC-5 처리 흐름 — 구성원 초대 및 합류

---

## 1. 초대 코드 발급

`POST /api/v1/groups/{groupId}/invite`

1. JWT → memberId 추출
2. group 조회 (없으면 404)
3. groupMember 조회 → HOST 확인 (아니면 403)
4. 기존 초대 코드 삭제 (groupId 기준)
5. UUID 코드 생성, `expires_at = now + 48h`
6. group_invite 저장
7. 201 Created + inviteCode 반환

---

## 2. 초대 코드로 합류

`POST /api/v1/groups/join`

1. JWT → memberId 추출
2. group_invite 조회 (code) — 없으면 404
3. `invite.isExpired()` 확인 — 만료면 400
4. groupMember 조회 (groupId + memberId) — 이미 있으면 400
5. `GroupMember.createMember(groupId, memberId)` 저장 (`role=MEMBER`, `attendanceStatus=JOIN`)
6. 해당 그룹의 최신 meeting 조회
   - meeting이 존재하고 CLOSED가 아니면 → meeting_participant 생성
     - 기본 출발지 있으면 lat/lng 채움
     - 없으면 lat/lng = null
7. 200 OK

---

## 연관 테이블 읽기/쓰기

| 테이블 | 발급 | 합류 |
|---|---|---|
| `group_info` | 읽기 (존재 확인) | — |
| `group_member` | 읽기 (HOST 검증) | **INSERT** |
| `group_invite` | DELETE + **INSERT** | 읽기 (코드 검증) |
| `meeting` | — | 읽기 (활성 모임 확인) |
| `departure_place` | — | 읽기 (기본 출발지) |
| `meeting_participant` | — | **INSERT** (활성 모임 있을 때) |
