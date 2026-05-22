# Functional Design Plan — Unit 3 (FC-6) 모임 리스트

## 분석 요약

- **PRD 출처**: docs/prd/mvp1.md FC-6 섹션
- **의존 테이블**: group_info, meeting, group_member, member (FC-4에서 생성, 신규 테이블 없음)
- **신규 Flyway 마이그레이션**: 불필요
- **핵심 로직**: JWT memberId → 소속 그룹 → 각 그룹의 현재 모임 → 상태 계산 → 정렬 → 응답

## 실행 단계

- [x] Step 1: 질문 파일 생성 및 답변 수령
- [x] Step 2: business-logic-model.md 생성
- [x] Step 3: business-rules.md 생성
- [x] Step 4: domain-entities.md 생성
- [x] Step 5: erd.md 생성 (review)
- [x] Step 6: rules.md 생성 (review)
- [x] Step 7: project-erd.md 업데이트

## 설계 결정 사항 (질문 전 확정)

| 항목 | 결정 |
|---|---|
| MeetingListStatus 계산 | 기존 Meeting.computeListStatus() 재사용 |
| 신규 테이블 | 없음 (기존 테이블만 조회) |
| 정렬 | IN_PROGRESS → CONFIRMED → CLOSED, 동일 상태 내 meeting.created_at DESC |
| locationAddress | nullable String (장소 선정은 MVP2, null 반환) |

---

# Functional Design 질문 파일 — FC-6

PRD와 기존 코드를 분석한 결과, 아래 3가지 항목의 결정이 필요합니다.

## Question 1
FC-6 API 엔드포인트를 어느 컨텍스트에 둘까요?
홈 화면은 사용자 눈에 "모임" 목록이지만, 내부는 그룹 멤버십 기반 조회입니다.

A) `GET /api/v1/meetings` — meeting 컨텍스트 (MeetingController 신규 생성)
B) `GET /api/v1/groups/meetings` — group 컨텍스트 (기존 GroupController 확장)

[Answer]: A

## Question 2
`group_info.status = CLOSED` (호스트가 직접 종료한 그룹)의 모임도 홈 화면 리스트에 포함할까요?
(FC-8에서 그룹 종료 기능 구현 예정, 지금 설계에 반영 필요)

A) 포함 — CLOSED 그룹의 모임도 CLOSED 상태로 리스트 하단에 노출
B) 제외 — GroupStatus.ACTIVE인 그룹의 모임만 조회

[Answer]: A

## Question 3
구성원 목록 응답에서 **본인(요청자)**의 위치를 어떻게 처리할까요?
PRD 모임 상세에는 "내 정보" 별도 표시가 있는데, 리스트 카드에서도 구분이 필요한지요.

A) 구분 없음 — members 배열에 본인도 동일하게 포함 (카드에서 iOS가 처리)
B) 별도 필드 — `me: {...}` 와 `members: [...]` 로 분리해서 응답

[Answer]: 
