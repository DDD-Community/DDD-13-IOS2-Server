# Functional Design Plan — Unit 4 (FC-7-1) 내 정보 수정

## 분석 요약

- **PRD 출처**: docs/prd/mvp1.md FC-7-1 섹션
- **의존 테이블**: group_member (참석여부), departure_place (출발지) — 신규 테이블 없음
- **신규 Flyway 마이그레이션**: 불필요
- **핵심 로직**:
  - 참석여부: JWT memberId + groupId → group_member 조회 → attendance_status 업데이트
  - 출발지: 최대 3개 제한, 추가(POST) / 수정(PUT)

## 실행 단계

- [x] Step 1: 질문 답변 수령
- [x] Step 2: business-logic-model.md 생성
- [x] Step 3: business-rules.md 생성
- [x] Step 4: domain-entities.md 생성
- [x] Step 5: erd.md 생성 (review)
- [x] Step 6: rules.md 생성 (review)
- [x] Step 7: project-erd.md 업데이트

## 설계 결정 사항 (질문 전 확정)

| 항목 | 결정 |
|---|---|
| 출발지 최대 개수 | 3개 (PRD 명시, 기존 코드 MAX_PLACES=10 → 수정 필요) |
| 본인만 수정 가능 | JWT memberId와 수정 대상 일치 여부를 서버에서 검증 |
| 신규 테이블 | 없음 (group_member, departure_place 기존 테이블 사용) |

---

# Functional Design 질문 파일 — FC-7-1

PRD와 기존 코드를 분석한 결과, 아래 4가지 항목의 결정이 필요합니다.

## Question 1
참석여부 수정 API 경로를 어떻게 설계할까요?
`attendance_status`는 `group_member` 테이블에 저장되어 그룹 단위로 관리됩니다.

A) `PATCH /api/v1/groups/{groupId}/members/me/attendance` — 그룹 기준 (PRD "그룹 상세 내" 문맥)
B) `PATCH /api/v1/meetings/{meetingId}/members/me/attendance` — 모임 기준

[Answer]: A

---

## Question 2
출발지 추가(`POST /api/v1/departure-places`) 요청 시 `isDefault` 값을 어떻게 처리할까요?

A) 클라이언트가 `isDefault: true/false` 명시 — iOS가 원하는 출발지를 기본으로 지정
B) 서버 자동 처리 — 첫 번째 등록 시만 자동으로 default, 이후 추가는 non-default

[Answer]:  B // A , 클라에서 송신 시 BE에서 방어 로직 작성 필요 

---

## Question 3
출발지 수정(`PUT /api/v1/departure-places/{id}`) 시 변경 가능한 필드 범위는?

A) label + address + latitude + longitude 모두 변경 가능 (장소 검색 창에서 전체 재선택)
B) address + latitude + longitude만 변경 (label은 별도 수정 불가)

[Answer]:  A

---

## Question 4
출발지 수정(`PUT`) 시 `isDefault` 값도 변경 가능한가요?

A) 가능 — `isDefault: true`로 수정하면 해당 출발지가 기본으로 설정되고 기존 기본 해제
B) 불가 — 출발지 수정은 장소 정보(label/address/좌표)만, 기본 설정 변경은 별도 API

[Answer]: B
