# Domain Entities — Unit 1 (FC-4)

## group 바운디드 컨텍스트

### Group — 그룹 도메인 모델

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK, auto increment |
| name | String | 그룹명. 모임명과 동일한 값으로 저장됨. 최대 30자 |
| themeTag | ThemeTag | 테마 태그. 8개 enum 중 하나 (비즈니스, 친목 등) |
| status | GroupStatus | 그룹 운영 상태. 생성 시 ACTIVE, 호스트가 종료하면 CLOSED |
| createdAt | LocalDateTime | 그룹 생성 시각 |
| updatedAt | LocalDateTime | 마지막 수정 시각 |

**정적 팩토리**: `Group.create(name, themeTag)` → 이름 30자 초과 시 BusinessException

---

### GroupMember — 그룹 내 구성원

그룹에 속한 사람 한 명을 나타내는 엔티티. 같은 사람이 같은 그룹에 두 번 들어올 수 없으므로 `(groupId, memberId)` 조합은 유니크 제약.

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK, auto increment (JPA 단순화를 위해 surrogate key 사용) |
| groupId | Long | 소속 그룹 FK |
| memberId | Long | 구성원 FK (auth.member 테이블 참조) |
| role | GroupMemberRole | 그룹 내 역할. 그룹 생성자는 HOST, 초대로 들어온 사람은 MEMBER |
| attendanceStatus | AttendanceStatus | 참석여부. 생성 시 JOIN(참여)으로 초기화, 본인이 직접 변경 가능 |
| joinedAt | LocalDateTime | 그룹에 합류한 시각 |

> `(groupId, memberId)` 조합에 UNIQUE 제약 적용 → 동일 그룹 중복 가입 방지

**정적 팩토리**: `GroupMember.createHost(groupId, memberId)` → role=HOST, attendanceStatus=JOIN으로 생성

---

### ThemeTag (enum) — 모임 테마

| 값 | 화면 표시 |
|---|---|
| BUSINESS | 비즈니스 |
| SOCIAL | 친목 |
| FAMILY | 가족모임 |
| DINING | 회식 |
| CASUAL_MEAL | 간단한 식사 |
| STUDY | 스터디 |
| BIRTHDAY | 생일파티 |
| WEDDING | 청첩장 모임 |

---

### GroupStatus (enum)

| 값 | 의미 |
|---|---|
| ACTIVE | 운영 중인 그룹 |
| CLOSED | 호스트가 종료한 그룹 |

### GroupMemberRole (enum)

| 값 | 의미 |
|---|---|
| HOST | 그룹을 만들었거나 위임받은 사람. 그룹 종료·새 모임 시작·날짜 확정 권한 보유 |
| MEMBER | 초대 링크로 합류한 일반 구성원 |

### AttendanceStatus (enum)

| 값 | 화면 표시 |
|---|---|
| JOIN | 참여 |
| LATE | 늦참 |
| ABSENT | 불참 |

---

## meeting 바운디드 컨텍스트

### Meeting — 모임 도메인 모델

그룹 안에서 반복 생성되는 모임 단위. 날짜 투표와 장소 선정 상태를 독립적으로 관리함.

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK, auto increment |
| groupId | Long | 소속 그룹 FK |
| name | String | 모임명. 그룹명과 동일한 값. 최대 30자 |
| themeTag | ThemeTag | 테마 태그. 그룹의 themeTag와 동일한 값으로 생성 |
| locationStatus | LocationStatus | 장소 선정 진행 상태. 초기값 BEFORE(선정 전) |
| dateVoteStatus | DateVoteStatus | 날짜 투표 진행 상태. 초기값 BEFORE(선정 전) |
| confirmedDate | LocalDate | 확정된 모임 날짜. 날짜가 정해지기 전까지는 null |
| createdAt | LocalDateTime | 모임 생성 시각 |
| updatedAt | LocalDateTime | 마지막 수정 시각 |

**정적 팩토리**: `Meeting.createFirst(groupId, name, themeTag)` → locationStatus·dateVoteStatus 모두 BEFORE로 생성

**도메인 메서드**: `MeetingListStatus computeListStatus(LocalDate today)` → 홈 화면 목록에서 카드 상태 계산 (DB 컬럼 없이 위 필드들로 계산)

---

### LocationStatus (enum) — 장소 선정 상태

| 값 | 화면 표시 |
|---|---|
| BEFORE | 선정 전 |
| IN_PROGRESS | 선정 중 |
| COMPLETED | 선정 완료 |

### DateVoteStatus (enum) — 날짜 투표 상태

| 값 | 화면 표시 |
|---|---|
| BEFORE | 선정 전 |
| IN_PROGRESS | 선정 중 (투표 진행 중) |
| COMPLETED | 선정 완료 (날짜 확정됨) |

### MeetingListStatus (enum) — 홈 화면 카드 표시 상태 (DB 저장 안 함, Java 계산용)

| 값 | 계산 조건 |
|---|---|
| CLOSED | confirmedDate != null && confirmedDate < 오늘 → 모임 날짜가 지남 |
| IN_PROGRESS | locationStatus 또는 dateVoteStatus 중 하나라도 IN_PROGRESS |
| CONFIRMED | locationStatus == COMPLETED && dateVoteStatus == COMPLETED |

> **판단 순서**: CLOSED 먼저 확인 → IN_PROGRESS → CONFIRMED
> (날짜가 지났으면 다른 상태와 무관하게 CLOSED)
