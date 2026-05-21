# Functional Design Plan — Unit 1 (FC-4) 그룹 & 첫 모임 생성

## 유닛 요약
호스트가 모임 이름과 테마 태그를 입력하면 **그룹(Group)과 첫 번째 모임(Meeting)이 동시에 생성**되고,
생성자가 자동으로 HOST Membership을 부여받는다.

---

## 도메인 모델 초안 (확인 필요)

### group 컨텍스트

**Group**
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK |
| name | String | 그룹명 (최대 30자) |
| themeTag | ThemeTag | 테마 태그 enum |
| status | GroupStatus | ACTIVE / CLOSED |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

**Membership** (그룹 내 구성원)
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK |
| groupId | Long | FK |
| memberId | Long | FK (auth.Member) |
| role | MembershipRole | HOST / MEMBER |
| attendanceStatus | AttendanceStatus | 참여 / 늦참 / 불참 (초기값: 참여) |
| joinedAt | LocalDateTime | |

**ThemeTag enum** (고정 8개)
- BUSINESS(비즈니스), SOCIAL(친목), FAMILY(가족모임), DINING(회식),
  CASUAL_MEAL(간단한 식사), STUDY(스터디), BIRTHDAY(생일파티), WEDDING(청첩장 모임)

**GroupStatus enum**: ACTIVE, CLOSED

**MembershipRole enum**: HOST, MEMBER

**AttendanceStatus enum**: JOIN(참여), LATE(늦참), ABSENT(불참)

---

### meeting 컨텍스트

**Meeting**
| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK |
| groupId | Long | FK |
| name | String | 모임명 = 그룹명 (최대 30자) |
| themeTag | ThemeTag | 그룹과 동일값 |
| locationStatus | LocationStatus | 선정 전 / 선정 중 / 선정 완료 (초기: 선정 전) |
| dateVoteStatus | DateVoteStatus | 선정 전 / 선정 중 / 선정 완료 (초기: 선정 전) |
| confirmedDate | LocalDate | nullable (날짜 확정 후 저장) |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

**LocationStatus enum**: BEFORE(선정 전), IN_PROGRESS(선정 중), COMPLETED(선정 완료)
**DateVoteStatus enum**: BEFORE(선정 전), IN_PROGRESS(선정 중), COMPLETED(선정 완료)

---

## 비즈니스 규칙 초안

1. 그룹명 = 모임명 (동일한 name 문자열, 각각 저장)
2. 이름 최대 30자 (초과 시 400 에러)
3. 그룹 생성자 → Membership(role=HOST, attendanceStatus=JOIN) 자동 생성
4. 그룹 최대 구성원 20명 (초대 시 검증, 생성 시는 해당 없음)
5. Group + Meeting + Membership 세 엔티티는 **단일 트랜잭션**으로 생성

---

## 설계 질문 (답변 필요)

아래 [Answer]: 태그에 직접 기입해주세요.

---

### Q1: 트랜잭션 담당 서비스

Group + Meeting + Membership 동시 생성 트랜잭션을 어느 서비스가 담당하나요?

**옵션 A**: `GroupService` 단일 서비스가 GroupRepository + MeetingRepository 모두 의존
  → group 컨텍스트가 meeting 컨텍스트에 직접 의존 (컨텍스트 간 의존 발생)

**옵션 B**: `GroupService`가 그룹·멤버십만 생성, 생성 후 `MeetingService`를 호출
  → 서비스 간 호출, 두 서비스가 각각 @Transactional이면 트랜잭션 분리됨

**옵션 C**: Presentation 레이어 Controller에서 두 서비스 호출 후 응답 조합
  → 트랜잭션 원자성 보장 어려움

[Answer]:

---

### Q2: MeetingStatus 계산 방식

모임 리스트(FC-6) 상태 정렬에 필요한 MeetingStatus(진행 중/확정/종료).

**옵션 A**: DB에 `MeetingStatus` enum 컬럼 저장, 상태 변경 시 명시적 업데이트
  → 쿼리 단순, 정렬 인덱스 활용 가능

**옵션 B**: 런타임 계산 — locationStatus + dateVoteStatus + confirmedDate + 현재시각으로 도메인 메서드 계산
  → DB 컬럼 없음, 항상 최신 상태 보장, 대량 조회 시 N+1 주의

[Answer]:

---

### Q3: API 응답 형태 (FC-4 생성 API)

`POST /api/v1/groups` 응답에 포함할 데이터.

**옵션 A**: 그룹 + 모임 기본 정보 (groupId, meetingId, name, themeTag)
**옵션 B**: 모임 상세 화면 바로 진입 가능한 전체 정보 포함 (FC-7 상세 수준)

[Answer]:

---

## 답변 후 생성 예정 아티팩트

- `domain-entities.md` — 도메인 모델 확정본
- `business-rules.md` — 비즈니스 규칙 확정본
- `business-logic-model.md` — 생성 플로우 상세 (Group→Meeting→Membership 순서, 검증 로직)
