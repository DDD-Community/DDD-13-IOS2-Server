# Business Logic Model — FC-7-1 내 정보 수정

## 1. 참석여부 수정

### 흐름
```
JWT → memberId 추출
  └─ groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
       ├─ 없음 → 403 NOT_GROUP_MEMBER
       └─ 있음 → groupMember.updateAttendance(newStatus)
                  └─ groupMemberRepository.save(groupMember) → 200 OK
```

### 핵심 결정
- `attendance_status`는 `group_member` 테이블에 저장 (그룹 단위, meeting과 무관)
- 본인 소유 검증: `findByGroupIdAndMemberId(groupId, JWT.memberId)` 로 한 번에 처리

---

## 2. 출발지 추가

### 흐름
```
JWT → memberId 추출
  └─ countByMemberId(memberId)
       ├─ 3개 이상 → 400 DEPARTURE_PLACE_LIMIT_EXCEEDED
       └─ 3개 미만
            ├─ count == 0 → isDefault 강제 true (방어 로직)
            ├─ request.isDefault == true → clearDefaultByMemberId(memberId) 후 저장
            └─ request.isDefault == false → 그대로 저장
          └─ repository.save(DeparturePlace) → 201 Created
```

### 핵심 결정
- 최대 3개 (PRD 명시, 기존 MAX_PLACES=10 → 3으로 수정)
- 클라이언트가 isDefault 명시 (Q2=A), 단 첫 등록 시 서버가 강제 true (방어)
- isDefault=true 요청 시 기존 default 자동 해제

---

## 3. 출발지 수정

### 흐름
```
JWT → memberId 추출
  └─ findByIdAndMemberId(id, memberId)
       ├─ 없음 → 404 DEPARTURE_PLACE_NOT_FOUND (본인 소유 검증 겸용)
       └─ 있음 → place.update(label, address, coordinate)
                  └─ repository.save(place) → 200 OK
```

### 핵심 결정
- 변경 가능 필드: label + address + latitude + longitude (Q3=A)
- isDefault는 수정 불가 (Q4=B) — 장소 정보만 변경
- `findByIdAndMemberId`로 본인 소유 검증과 조회를 한 번에 처리
