# Domain Entities — FC-6 모임 리스트

## 신규 도메인 엔티티

없음 — 기존 도메인 모델 재사용

---

## 신규 애플리케이션 서비스

### MeetingListService (`meeting/application/`)

```
의존성:
  - GroupMemberRepository  (group context)
  - GroupRepository        (group context)
  - MeetingRepository      (meeting context)
  - MemberRepository       (auth/member context)
  - ThemeTagRepository     (group context)

메서드:
  + getMyMeetingList(memberId: Long): List<MeetingCardResponse>
```

---

## 신규 Repository 메서드

### MeetingRepository (meeting/domain/)

```java
// 그룹당 가장 최신 meeting 1개씩 조회
List<Meeting> findLatestByGroupIdIn(List<Long> groupIds);
// 구현: JPQL or @Query (DISTINCT ON group_id ORDER BY id DESC)
```

### GroupMemberRepository (group/domain/)

```java
// 복수 그룹의 전체 구성원 일괄 조회 (기존 메서드 확인 후 없으면 추가)
List<GroupMember> findByGroupIdIn(List<Long> groupIds);
```

### ThemeTagRepository (group/domain/)

```java
// 복수 코드로 테마 태그 일괄 조회 (없으면 추가)
List<ThemeTag> findByCodeIn(Set<String> codes);
```

---

## 신규 DTO

### MeetingCardResponse (`meeting/presentation/dto/`)

```
groupId         : Long
meetingId       : Long
name            : String         // 모임명 (= 그룹명)
themeTagCode    : String         // 예: "DINING"
themeTagDisplay : String         // 예: "회식"
listStatus      : String         // IN_PROGRESS | CONFIRMED | CLOSED
locationStatus  : String         // BEFORE | IN_PROGRESS | COMPLETED
dateVoteStatus  : String         // BEFORE | IN_PROGRESS | COMPLETED
locationAddress : String (null)  // MVP2 미구현, 항상 null
memberCount     : int
members         : List<MemberInfo>

inner MemberInfo:
  memberId         : Long
  nickname         : String
  profileImageUrl  : String
  attendanceStatus : String      // JOIN | LATE | ABSENT
```

---

## 신규 컨트롤러

### MeetingController (`meeting/presentation/`)

```
GET /api/v1/meetings
  → MeetingListService.getMyMeetingList(memberId)
  → 200 OK, List<MeetingCardResponse>
```
