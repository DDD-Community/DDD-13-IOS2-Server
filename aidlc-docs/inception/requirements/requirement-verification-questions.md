# Requirements Clarification Questions — MVP1

PRD(docs/prd/mvp1.md) 기반으로 백엔드 구현에 필요한 결정사항입니다.
각 질문의 `[Answer]:` 태그 뒤에 알파벳으로 답변해주세요.

---

## Question 1
날짜 투표 현황(FC-7)은 "실시간 또는 준실시간"으로 표시된다고 명시됩니다. 백엔드 구현 방식은?

A) 클라이언트 Polling — 앱이 주기적으로 REST API 호출 (구현 단순, MVP 적합)
B) SSE(Server-Sent Events) — 서버가 이벤트 스트림 푸시 (단방향 실시간)
C) WebSocket — 양방향 실시간 (구현 복잡도 높음)
D) Other (please describe after [Answer]: tag below)

[Answer]: 실시간으로 표시된다고 표기는 해놨는데 사실 단순 날짜 투표를 실시간으로 하기위에서 웹소켓으로 만드는게 맞나 싶네 리소스 너무 차지하는게 아닌가 싶고? 이거는 너의 설명이 필요하니 설명해주고 다시 정하기로 해보자

---

## Question 2
초대 링크(FC-5) 생성 방식은? 서버가 링크를 생성하고 iOS가 카카오톡으로 전송합니다.

A) 서버가 단순 토큰 발급 (예: `/invite/{token}`) — iOS가 딥링크 URL 조합해서 공유
B) 서버가 완성된 딥링크 URL 반환 (예: `bangawo://invite?token=xxx`)
C) 서버가 완성된 카카오 공유 URL까지 생성
D) Other (please describe after [Answer]: tag below)

[Answer]: D 아마 IOS쪽에서 서버로 모임 코드와 함꼐 던져줄꺼야 그럼 그 초대코드 받고 해당 그룹 페이지로 이동할 수 있게 IOS로 다시 던져주면 될텐데?

---

## Question 3
푸시 알림(FC-7 투표 시작·마감·확정 알림)을 MVP1에서 실제로 구현하나요? device_token 테이블은 이미 존재합니다.

A) 실제 FCM 연동까지 완전 구현 (서버 → FCM → iOS 디바이스)
B) 알림 전송 로직만 구현 (FCM 키 설정은 나중에)
C) MVP1에서는 스킵 — 데이터 변경 API만 구현, 알림은 MVP2
D) Other (please describe after [Answer]: tag below)

[Answer]: A 해야하는데 FCM 이 뭔지 잘 모르겠어서 추가 설명 해주고 필요한게있으면 따로 뺴서 추가 구현할수있게 계획, 일정 짜줘야해

---

## Question 4
모임 자동 종료(FC-8 "모임 날짜가 지나면 자동 종료") 처리 방식은?

A) 스케줄러(@Scheduled) — 매일 자정 배치로 상태 업데이트
B) 조회 시점 Lazy 계산 — 리스트 조회 시 날짜 비교해서 status 계산 (DB 상태값 미변경)
C) 두 가지 모두 — 조회는 Lazy, 배치로 주기적 정리
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 5
FC-6 모임 리스트의 상태(진행 중/확정/종료) 정렬 시, 그룹에 "현재 진행 중인 모임"이 없는 경우 (모임이 아직 생성 안 됨) 어떻게 처리하나요?

A) 그룹 생성 시 모임도 자동으로 함께 생성 (그룹과 모임 항상 1:1로 존재)
B) 그룹만 생성, 모임은 호스트가 별도로 시작할 때 생성
C) Other (please describe after [Answer]: tag below)

[Answer]: C PRD에 나와있어. 그룹 생성하면 첫번째 모임도 자동으로 생성되고 모임마다 날짜가 있으면 그날이 지나면 그 모임은 종료되도록 할꺼야. 화면에서는 그룹 = 모임 같은 용도로쓸꺼고 모임이 종료되면 종료됨으로 표시될꺼고 그 그룹(모임) 들어가면 다시 모임을 만들 수 있게 해야겠지.

---

## Question 6 (Extension Opt-In)
보안 확장(Security Baseline) 규칙을 이 프로젝트에 적용할까요?
— JWT/인증 이미 구현되어 있으나 group/meeting API에 대한 보안 검증 룰 추가 적용 여부

A) Yes — 보안 규칙을 블로킹 제약으로 적용 (프로덕션 수준)
B) No — 스킵 (MVP 빠른 구현 우선)
C) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 7 (Extension Opt-In)
TDD 방식으로 코드를 생성할까요? (테스트 먼저 작성 후 구현)

A) Yes — TDD 워크플로우 적용 (토큰 비용 1.5~2배, 결함 최소화)
B) No — 표준 코드 생성 (빠른 구현)
C) Other (please describe after [Answer]: tag below)

[Answer]: C 주요 기능들은 테스트 하도록하고 중요하지않은건 스킵

---

## Question 8 (Extension Opt-In)
Property-Based Testing 규칙을 적용할까요?

A) Yes — 전체 적용
B) Partial — 순수 함수·직렬화에만 적용
C) No — 스킵
D) Other (please describe after [Answer]: tag below)

[Answer]: C
