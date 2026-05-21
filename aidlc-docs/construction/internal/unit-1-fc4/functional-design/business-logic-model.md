# Business Logic Model — Unit 1 (FC-4)

## 그룹 & 첫 모임 생성 플로우

```
POST /api/v1/groups
  Body: { name, themeTag }
  Auth: JWT 필수

1. GroupController
   └── 요청 수신, JWT에서 memberId 추출

2. GroupService.createGroupWithMeeting(memberId, name, themeTag)  [@Transactional]
   ├── 2-1. 이름 길이 검증 (> 30자 → BusinessException)
   ├── 2-2. Group.create(name, themeTag) → Group 저장
   ├── 2-3. Meeting.createFirst(group.id, name, themeTag) → Meeting 저장
   ├── 2-4. GroupMember.createHost(group.id, memberId) → GroupMember 저장
   └── 2-5. CreateGroupResponse(groupId, meetingId, name, themeTag) 반환

3. GroupController
   └── HTTP 201 Created + CreateGroupResponse 반환
```

## 응답 데이터

```
CreateGroupResponse
├── groupId: Long
├── meetingId: Long    ← 앱이 모임 상세 화면으로 이동하는 데 사용
├── name: String
└── themeTag: ThemeTag
```

## 생성되는 DB 레코드

| 테이블 | 생성 내용 |
|---|---|
| `group_info` | 그룹 1건 (name, themeTag, status=ACTIVE) |
| `meeting` | 모임 1건 (groupId, name, themeTag, locationStatus=BEFORE, dateVoteStatus=BEFORE) |
| `group_member` | 멤버십 1건 (groupId, memberId, role=HOST, attendanceStatus=JOIN) |

## 관련 ErrorCode 추가 필요

| 코드 | HTTP | 메시지 |
|---|---|---|
| `GROUP_NAME_TOO_LONG` | 400 | 그룹명은 30자 이하여야 합니다 |
| `GROUP_NOT_FOUND` | 404 | 그룹을 찾을 수 없습니다 |
| `NOT_GROUP_MEMBER` | 403 | 해당 그룹의 구성원이 아닙니다 |
| `NOT_GROUP_HOST` | 403 | 호스트만 수행할 수 있습니다 |
