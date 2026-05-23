# Unit 4 (FC-7) Functional Design Plan

## 분석 완료 사항

- PRD FC-7 읽음: 모임 상세, 날짜 투표 방식 A/B, SSE 실시간 현황, 자동 종료 스케줄러
- 기존 코드 분석 완료:
  - `meeting` 도메인: `Meeting`, `DateVoteStatus`, `LocationStatus`, `MeetingListStatus`
  - `group` 도메인: `GroupMember`, `AttendanceStatus`, `GroupMemberRole`
  - V7 마이그레이션: `meeting`, `group_info`, `group_member`, `theme_tag`
  - V3 마이그레이션: `departure_place` (member 전역 테이블, member_id 연결)
  - FC-6 완료 코드: `MeetingController`, `MeetingListService`, `MeetingCardResponse`

## 계획 체크리스트

- [x] Step 1: 기존 아티팩트 분석 (RE + RA + FC-6 코드)
- [ ] Step 2: 설계 질문 작성
- [ ] Step 3: 설계 질문 답변 수집
- [ ] Step 4: 도메인 엔티티 설계 (DateVoteSession, DateVoteOption, DateVoteRecord)
- [ ] Step 5: 비즈니스 로직 모델 설계 (상태머신, 투표 플로우)
- [ ] Step 6: 비즈니스 규칙 설계 (인가, 제약, 예외 케이스)
- [ ] Step 7: ERD 생성 (`aidlc-docs/construction/review/fc-7/erd.md`, `rules.md`)
- [ ] Step 8: 마스터 ERD 업데이트 (`aidlc-docs/construction/review/project-erd.md`)

## 신규 도메인 모델 초안

### 예상 신규 테이블

| 테이블 | 설명 |
|---|---|
| `date_vote_session` | 투표 세션 (meeting당 1개, method + deadline 포함) |
| `date_vote_option` | 후보 날짜 (session당 최대 3개) |
| `date_vote_record` | 투표 기록 (option당 N개, member당 복수 선택 가능) |

### 기존 테이블 변경 가능성

- `group_member`: 출발지 설계 방향에 따라 변경 여부 결정
- `meeting`: 투표 세션 저장 방식에 따라 컬럼 추가 가능

---

## 설계 질문

아래 질문에 답변해주세요. 각 [Answer]: 태그 뒤에 선택지 알파벳을 입력하세요.

## Question 1
모임 상세 화면에서 참여인원의 "출발지" 표시 방식을 어떻게 설계할까요?

현재 `departure_place` 테이블은 `member`에 연결된 전역 출발지(V3)입니다.
FC-7-1에서 "출발지는 최대 3개까지 등록 가능"하며 날짜 투표 종료 후 수정 잠금.

A) member의 기본 출발지(is_default = true) 1개만 표시 (테이블 변경 없음)
B) member의 출발지 전체 목록 표시 (최대 3개, 테이블 변경 없음)
C) group_member별 별도 출발지 관리 — departure_place와 별개로 `group_member_departure` 신규 테이블 추가
D) Other (please describe after [Answer]: tag below)

[Answer]: B → 수정: member의 출발지 전체 목록 표시 (N개, "최대 3개" 제한 없음)

## Question 2
날짜 투표 방식 B의 세션 정보(마감일, 투표 방식)를 어디에 저장할까요?

A) `date_vote_session` 별도 테이블 (meeting:session = 1:1)
B) `meeting` 테이블에 컬럼 추가 (`vote_deadline`, `vote_method` 등)
C) Other (please describe after [Answer]: tag below)

[Answer]: A — date_vote_session 별도 테이블 (MVP2 장소 투표 세션 추가 시 meeting 컬럼 폭증 방지)

## Question 3
방식 A(호스트 단독 선택) API 플로우는?

PRD: "호스트가 달력에서 날짜 1개 선택 → 별도 알림 없이 즉시 확정"

A) 단일 API — POST /api/v1/meetings/{meetingId}/date-vote/host-pick (날짜 전달 즉시 confirmed_date 설정 + COMPLETED)
B) 방식 B 시작 API와 동일 엔드포인트 공유, 후보 1개이면 즉시 자동 확정 처리
C) Other (please describe after [Answer]: tag below)

[Answer]: A — 별도 API 2개 (POST /date-vote/host-pick, POST /date-vote). DDD 유스케이스 명확히 분리.

## Question 4
투표 자동 확정(마감일 도래 시 1위 날짜 자동 확정) 처리 주기는?

A) 모임 자동 종료와 같은 @Scheduled 배치(매일 자정)에서 함께 처리
B) 별도 @Scheduled (매 시간 또는 더 짧은 주기)로 더 정확한 마감 처리
C) Other (please describe after [Answer]: tag below)

[Answer]:A (참고로 투표 마감일을 정해주는게 아니라 1,3,7처럼 N 숫자를 던져주면 지금 날짜 기준으로 N일 뒤까지라 그날 자정에 투표 마감되도록 스케쥴러 정의하면 됨)

## Question 5
SSE 구독 엔드포인트 URL 형태는?

A) GET /api/v1/meetings/{meetingId}/date-vote/stream
B) GET /api/v1/meetings/{meetingId}/stream (모임 전체 실시간 이벤트)
C) Other (please describe after [Answer]: tag below)

[Answer]: SSE 미사용 — GET /api/v1/meetings/{meetingId}/date-vote 폴링 방식. requirements.md NFR-1 + FR-5 수정 완료.
