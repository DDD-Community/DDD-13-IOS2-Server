# FC-7-1 처리 흐름 — 내 정보 수정

---

## 1. 참석여부 수정

`PATCH /api/v1/groups/{groupId}/members/me/attendance`  
body: `{ "attendanceStatus": "LATE" }`

1. JWT → memberId 추출
2. groupMember 조회 (groupId + memberId) — 없으면 403
3. `groupMember.attendanceStatus = status` 업데이트
4. groupMember 저장
5. 200 OK (body 없음)

> 쓰는 테이블: `group_member.attendance_status`

---

## 2. 출발지 추가

`POST /api/v1/departure-places`

1. JWT → memberId 추출
2. `departure_place` 개수 조회 — 3개 이상이면 400
3. 현재 개수 == 0이면 `isDefault = true` 강제
4. `isDefault = true` 요청이면 기존 기본 출발지 `is_default = false` 일괄 해제
5. departure_place 저장
6. 201 Created + 저장된 출발지 반환

> 쓰는 테이블: `departure_place`

---

## 3. 출발지 수정

`PUT /api/v1/departure-places/{id}`

1. JWT → memberId 추출
2. departure_place 조회 (id + memberId) — 없거나 타인 소유면 404
3. label / address / latitude / longitude 업데이트
4. departure_place 저장
5. 200 OK + 수정된 출발지 반환

> isDefault는 이 API로 변경 불가 (setDefault API 별도)  
> 쓰는 테이블: `departure_place`

---

## 3. 모임별 출발지 변경

`PATCH /api/v1/meetings/{meetingId}/participants/me/departure`  
body: `{ "departurePlaceId": 2 }`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. departure_place 조회 (departurePlaceId + memberId) — 없거나 타인 소유면 404
4. meeting_participant 조회 (meetingId + memberId) — 없으면 404
5. `participant.latitude/longitude` 업데이트
6. meeting_participant 저장
7. 200 OK (body 없음)

> 쓰는 테이블: `meeting_participant`  
> `departure_place`는 읽기만 함 (좌표 참조용)

---

## 두 종류의 출발지 변경 비교

| 구분 | API | 쓰는 테이블 | 용도 |
|---|---|---|---|
| 내 출발지 수정 | `PUT /departure-places/{id}` | `departure_place` | 개인 출발지 목록 관리 |
| 모임 출발지 변경 | `PATCH /meetings/{id}/participants/me/departure` | `meeting_participant` | 이 모임에서 쓸 출발지 지정 |
