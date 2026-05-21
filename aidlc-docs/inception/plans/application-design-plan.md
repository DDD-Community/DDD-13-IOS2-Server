# Application Design Plan — Bangawo MVP1

## 범위
group, meeting 두 개 신규 바운디드 컨텍스트 설계.
기존 global, auth, member 컨텍스트 패턴 준수.

---

## 생성할 아티팩트 체크리스트

- [ ] `components.md` — 컴포넌트 정의 및 책임
- [ ] `component-methods.md` — 메서드 시그니처 (비즈니스 로직 상세는 Functional Design에서)
- [ ] `services.md` — 서비스 정의 및 오케스트레이션 패턴
- [ ] `component-dependency.md` — 의존성 관계 및 통신 패턴
- [ ] `application-design.md` — 위 문서 통합 요약

---

## 설계 범위 분석

### 신규 컴포넌트 목록 (초안)

**group 바운디드 컨텍스트**
- `Group` — 그룹 도메인 모델
- `Membership` — 그룹 내 구성원 (HOST/MEMBER 역할)
- `InviteCode` — 초대 코드 도메인 모델
- `GroupService` — 그룹 생성/초대/생명주기 유스케이스
- `GroupController` — /api/v1/groups REST API

**meeting 바운디드 컨텍스트**
- `Meeting` — 모임 도메인 모델
- `DateVote` — 날짜 투표 애그리거트
- `DateCandidate` — 날짜 후보 (DateVote 하위)
- `VoteRecord` — 개인 투표 기록
- `MeetingService` — 모임 유스케이스
- `DateVoteService` — 날짜 투표 유스케이스
- `MeetingController` — /api/v1/meetings REST API
- `MeetingScheduler` — @Scheduled 모임 자동 종료

**공통 인프라 (global 확장)**
- FCM 푸시 알림 — 위치 미결 (아래 Q1 참고)
- SSE 연결 관리 — 위치 미결 (아래 Q2 참고)

---

## 설계 질문 (답변 필요)

아래 [Answer]: 태그에 직접 답변을 기입해주세요.

---

### Q1: FCM 알림 컴포넌트 위치

FCM 푸시 알림은 투표 시작/마감/확정/그룹 초대 등 여러 컨텍스트에서 트리거됩니다.

**옵션 A**: `global/notification/` — 공통 유틸로 배치, 각 서비스에서 직접 호출
**옵션 B**: `notification/` 별도 바운디드 컨텍스트 — 독립적 패키지, 도메인 이벤트로 연결
**옵션 C**: `meeting/infrastructure/fcm/` — FCM은 meeting 컨텍스트 내부 인프라

[Answer]:

---

### Q2: SSE SseEmitter 관리 위치

날짜 투표 현황 SSE 스트림을 관리하는 컴포넌트 위치.

**옵션 A**: `meeting/infrastructure/sse/` — meeting 컨텍스트 내부 인프라로 배치
**옵션 B**: `global/sse/` — 향후 SSE 확장을 고려해 공통 인프라로 배치

[Answer]:

---

### Q3: MeetingStatus 저장 방식

모임 상태(진행 중/확정/종료)는 장소 상태 + 투표 상태 + 날짜 경과로 계산됩니다.

**옵션 A**: DB에 enum 컬럼으로 저장 — 변경 시 명시적 업데이트, 쿼리 단순
**옵션 B**: 런타임 계산 — DB 컬럼 없이 도메인 메서드로 계산, 데이터 정합성 자동 보장

[Answer]:

---

### Q4: 모임 상세 구성원 정보 조회 방식

모임 상세 화면에는 구성원의 프로필(이름, 이미지), 출발지, 참석여부가 필요합니다.
출발지는 `member` 컨텍스트에, 멤버십은 `group` 컨텍스트에 존재합니다.

**옵션 A**: `MeetingService`가 `MemberRepository`, `DeparturePlaceRepository`를 직접 의존
**옵션 B**: `MeetingService` → `MemberService` 애플리케이션 서비스 호출 (서비스 간 호출)
**옵션 C**: 모임 상세 조회는 `GroupService`와 `MeetingService`를 Presentation 레이어(Controller)에서 조합하여 DTO 합성

[Answer]:

---

### Q5: DateVote 애그리거트 경계

날짜 투표(`DateVote`), 날짜 후보(`DateCandidate`), 개인 투표 기록(`VoteRecord`)의 관계.

**옵션 A**: `DateVote` 루트 애그리거트, `DateCandidate` + `VoteRecord`를 내부 포함
  - Meeting 1:1 DateVote, DateVote 1:N DateCandidate, DateCandidate 1:N VoteRecord
**옵션 B**: `Meeting` 루트 애그리거트가 `DateVote`를 포함 (Meeting 안에 모두)
  - 복잡도가 높아질 수 있음

[Answer]:

---

### Q6: 초대 코드(InviteCode) 저장 방식

초대 코드는 UUID 또는 단순 랜덤 토큰으로 생성하고, 유효기간(2일 + 모임 종료일 이전) 검증이 필요합니다.

**옵션 A**: DB 테이블로 저장 (`invite_code` 테이블) — 만료·검증 이력 관리 가능
**옵션 B**: JWT에 그룹 ID + 만료시각 인코딩 — DB 없이 검증 가능, 단 revoke 불가

[Answer]:

---

### Q7: 날짜 투표 자동 종료/확정 스케줄러 위치

`@Scheduled` 로직이 두 종류입니다:
1. 모임 자동 종료 (날짜 지난 모임 CLOSED 처리)
2. 투표 자동 마감 + 1위 날짜 자동 확정

**옵션 A**: `MeetingScheduler` 단일 클래스에 두 가지 로직 통합
**옵션 B**: `MeetingScheduler`(모임 종료) + `DateVoteScheduler`(투표 마감) 분리

[Answer]:

---

## 답변 완료 후 진행 사항

모든 [Answer]: 태그 작성 완료 → Application Design 아티팩트 생성 → 승인 후 Units Generation
