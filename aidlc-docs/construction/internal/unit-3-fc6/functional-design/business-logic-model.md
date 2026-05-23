# Business Logic Model — FC-6 모임 리스트

## 핵심 유스케이스

**`getMyMeetingList(memberId)`** — 로그인 사용자의 모임 카드 목록을 반환한다.

---

## 조회 알고리즘

```
Step 1. JWT SecurityContext에서 memberId 추출

Step 2. GroupMemberRepository.findByMemberId(memberId)
        → List<GroupMember> (내가 속한 그룹의 멤버십 목록)
        → 비어있으면 빈 리스트 반환

Step 3. groupIds = 위 결과에서 groupId 추출 (Set<Long>)

Step 4. GroupRepository.findAllById(groupIds)
        → Map<groupId, Group>

Step 5. MeetingRepository.findLatestByGroupIdIn(groupIds)
        → 그룹당 가장 최근 생성된 Meeting 1개씩
        → Map<groupId, Meeting>
        → 구현: GROUP BY group_id 후 MAX(id) 기준 (PostgreSQL DISTINCT ON)

Step 6. GroupMemberRepository.findByGroupIdIn(groupIds)
        → 내 모든 그룹의 전체 구성원 목록 (나 포함)
        → Map<groupId, List<GroupMember>>

Step 7. allMemberIds = Step 6 결과에서 memberId 추출 (Set<Long>)
        MemberRepository.findAllById(allMemberIds)
        → Map<memberId, Member> (nickname, profileImageUrl)

Step 8. ThemeTagRepository.findByCodes(themeCodes)
        → Map<code, ThemeTag> (displayName 조회용)

Step 9. 각 그룹에 대해 MeetingCardResponse 조립:
        - name            ← Group.name
        - themeTagCode    ← Group.themeTagCode
        - themeTagDisplay ← ThemeTag.displayName
        - meetingId       ← Meeting.id
        - listStatus      ← Meeting.computeListStatus(LocalDate.now())
        - locationStatus  ← Meeting.locationStatus
        - dateVoteStatus  ← Meeting.dateVoteStatus
        - locationAddress ← null (MVP2)
        - memberCount     ← groupMembers[groupId].size()
        - members         ← groupMembers[groupId].map(gm →
                              MemberInfo(member[gm.memberId], gm.attendanceStatus))
                              * 합류 오래된 순(joinedAt ASC) 정렬

Step 10. 결과 리스트 정렬:
         Primary   : listStatus 우선순위 (IN_PROGRESS=1, CONFIRMED=2, CLOSED=3)
         Secondary : meeting.createdAt DESC (최신 모임이 위로)
```

---

## MeetingListStatus 계산 (기존 Meeting.computeListStatus() 재사용)

```
확정일(confirmedDate) < 오늘  →  CLOSED
locationStatus OR dateVoteStatus == IN_PROGRESS  →  IN_PROGRESS
locationStatus AND dateVoteStatus == COMPLETED  →  CONFIRMED
그 외 (BEFORE/BEFORE 등 초기 상태)  →  IN_PROGRESS
```

---

## 신규 Repository 메서드

| 메서드 | 위치 | 설명 |
|---|---|---|
| `findLatestByGroupIdIn(List<Long>)` | MeetingRepository | 그룹당 최신 Meeting 1개 |
| `findByGroupIdIn(List<Long>)` | GroupMemberRepository | 복수 그룹의 전체 구성원 |
| `findAllById(Set<Long>)` | MemberRepository | 회원 상세 일괄 조회 |
| `findByCodes(Set<String>)` | ThemeTagRepository | 테마 태그 일괄 조회 |

---

## N+1 방지 전략

- 그룹 조회, 모임 조회, 구성원 조회, 회원 조회를 **각 1회 배치 쿼리**로 처리
- 루프 안에서 개별 조회 금지
- 총 쿼리 수: 최대 6회 (고정)
