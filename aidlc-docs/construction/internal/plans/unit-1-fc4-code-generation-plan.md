# Code Generation Plan — Unit 1 (FC-4) 그룹 & 첫 모임 생성

## 구현 범위
`POST /api/v1/groups` — 그룹 + 첫 모임 + 호스트 멤버십을 단일 트랜잭션으로 생성

---

## 생성/수정 파일 목록

### 신규 생성 — group 컨텍스트
```
src/main/java/com/bangawo/group/
├── domain/
│   ├── Group.java
│   ├── GroupStatus.java          (enum)
│   ├── ThemeTag.java             (enum, meeting 컨텍스트도 공유)
│   ├── GroupMember.java
│   ├── GroupMemberRole.java       (enum)
│   ├── AttendanceStatus.java     (enum)
│   ├── GroupRepository.java      (인터페이스)
│   └── GroupMemberRepository.java (인터페이스)
├── application/
│   └── GroupService.java
├── presentation/
│   ├── GroupController.java
│   └── dto/
│       ├── CreateGroupRequest.java
│       └── CreateGroupResponse.java
└── infrastructure/persistence/
    ├── GroupJpaEntity.java
    ├── GroupJpaRepository.java
    ├── GroupRepositoryImpl.java
    ├── GroupMemberJpaEntity.java
    ├── GroupMemberJpaRepository.java
    └── GroupMemberRepositoryImpl.java
```

### 신규 생성 — meeting 컨텍스트 (FC-4 범위: Meeting 도메인 + 저장소만)
```
src/main/java/com/bangawo/meeting/
├── domain/
│   ├── Meeting.java
│   ├── LocationStatus.java   (enum)
│   ├── DateVoteStatus.java   (enum)
│   ├── MeetingListStatus.java (enum, 화면 전용)
│   └── MeetingRepository.java (인터페이스)
└── infrastructure/persistence/
    ├── MeetingJpaEntity.java
    ├── MeetingJpaRepository.java
    └── MeetingRepositoryImpl.java
```

### 수정 — 기존 파일
```
src/main/java/com/bangawo/global/error/ErrorCode.java  ← 에러코드 4개 추가
src/main/resources/db/migration/V7__create_group_meeting.sql  ← 신규 생성
```

---

## 실행 단계

- [ ] **Step 1** — enum 4종: `ThemeTag`, `GroupStatus`, `GroupMemberRole`, `AttendanceStatus`
- [ ] **Step 2** — 도메인 모델: `Group`, `GroupMember`
- [ ] **Step 3** — 리포지토리 인터페이스: `GroupRepository`, `GroupMemberRepository`
- [ ] **Step 4** — enum 3종: `LocationStatus`, `DateVoteStatus`, `MeetingListStatus`
- [ ] **Step 5** — 도메인 모델: `Meeting` (`computeListStatus()` 메서드 포함)
- [ ] **Step 6** — 리포지토리 인터페이스: `MeetingRepository`
- [ ] **Step 7** — `GroupService` (Group + Meeting + GroupMember 단일 트랜잭션 생성)
- [ ] **Step 8** — DTO: `CreateGroupRequest`, `CreateGroupResponse`
- [ ] **Step 9** — `GroupController` (`POST /api/v1/groups`)
- [ ] **Step 10** — JPA 엔티티: `GroupJpaEntity`, `GroupMemberJpaEntity`
- [ ] **Step 11** — JPA 리포지토리: `GroupJpaRepository`, `GroupMemberJpaRepository`
- [ ] **Step 12** — 구현체: `GroupRepositoryImpl`, `GroupMemberRepositoryImpl`
- [ ] **Step 13** — JPA 엔티티: `MeetingJpaEntity`
- [ ] **Step 14** — JPA 리포지토리: `MeetingJpaRepository`
- [ ] **Step 15** — 구현체: `MeetingRepositoryImpl`
- [ ] **Step 16** — `ErrorCode.java` 수정 (에러코드 4개 추가)
- [ ] **Step 17** — `V7__create_group_meeting.sql` (group_info, meeting, group_member 테이블)

---

## 주요 설계 결정 (코드 생성 기준)

| 항목 | 결정 |
|---|---|
| ThemeTag 위치 | `group.domain` — meeting 컨텍스트에서 import해서 사용 |
| GroupService 의존성 | GroupRepository + GroupMemberRepository + MeetingRepository |
| 트랜잭션 경계 | GroupService.createGroupWithMeeting() 단일 @Transactional |
| SecurityConfig | FC-4 API는 JWT 필수 — 기존 설정으로 자동 적용 (수정 불필요) |
| DB 테이블명 | group_info (group은 예약어), meeting, group_member |
