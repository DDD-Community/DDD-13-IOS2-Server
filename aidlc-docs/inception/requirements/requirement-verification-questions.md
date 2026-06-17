# Requirements Verification Questions — 장소 선정~확정 (FC-8~13)

> 각 질문의 `[Answer]:` 뒤에 답을 적어주세요. 객관식은 A/B/C 또는 X(직접 기술).
> 빠르게 가려면 추천안(✅)만 확인하고 "추천대로"라고 적어도 됩니다.

---

## A. 범위 / 진행 방식

### Q1. 이번 사이클 범위 (양이 많음 — 조절 가능)
FC-8~13 + 알림까지 한 번에 vs 분할.

- A) FC-8~13 전부 한 사이클로 설계 (Inception은 통째로, Construction은 유닛 분할) ✅추천
- B) 2분할 — (1차: FC-8 추천 + FC-9 담기) / (2차: FC-11~13 투표·확정)
- C) 3분할 — FC-8 / FC-9 / FC-11~13
- X) 기타

[Answer]: 너가 하기 괜찮은 context대로 해줘 그리고 지금 푸시알람기능은 고려도안하고 안할꺼니까 빼

### Q2. 알림(Push/In-App) 구현 범위
device_token(V5)은 있으나 실제 발송 로직 미확인.

- A) 이번 범위: 상태전환/마감 트리거 지점에 알림 발송 호출까지 구현(실제 FCM 등 연동 포함)
- B) 이번 범위: 알림 트리거 훅/이벤트만 마련, 실제 발송은 추후
- C) 알림 전체 이번 범위 제외 ✅추천(핵심 플로우 우선, 알림은 후속)
- X) 기타

[Answer]: C

---

## B. 상태 모델 (RE 발견 — 불일치)

### Q3. LocationStatus enum 재정의
현재 `BEFORE / IN_PROGRESS / COMPLETED` ↔ PRD `BEFORE / RECOMMENDED / VOTING / CONFIRMED`.

- A) PRD대로 4-state로 교체 (IN_PROGRESS→RECOMMENDED, 신규 VOTING, COMPLETED→CONFIRMED) ✅추천
- B) 기존 enum 유지하고 별도 단계 필드 추가
- X) 기타

[Answer]: A

---

## C. PRD §5 미결 사항

### Q4. (미결1) 담기 마감 도래 시 후보 0개(아무도 안 담음)
- A) 자동 종료 없이 호스트가 직접 장소 선택으로 전환
- B) 담기 마감 자동 연장(+N일)
- C) 모임 장소선정 자동 종료/취소
- D) 일단 에러/안내만 하고 호스트 재시작 유도 ✅추천(MVP 단순화)
- X) 기타

[Answer]: X 마감일 지나면 15개중에 가장 스코어에 근접한 3개정도 후보로 올려서 투표 진행

### Q5. (미결2) 자동 전환 시 투표 마감일 기본값
호스트가 마감일을 못 정한 채 VOTING 전환된 경우.

- A) +3일 프리셋 자동 적용 ✅추천
- B) +1일
- C) 모임일 직전(약속일 -1일)
- X) 기타

[Answer]: A

### Q6. (미결3) 이동 부담 산출 방식
subway_edge(V17) 그래프를 직접 만들어두심.

- A) subway_edge 그래프 최단경로(다익스트라)로 소요시간·환승 계산, 투표 시작 시 (참여자×후보) 1회 스냅샷 저장 ✅추천(PRD 우선안 일치)
- B) 실시간 계산(요청 시마다)
- C) 외부 길찾기 API 연동
- X) 기타

[Answer]: A (스냅샷을 어떻게 저장할껀지 궁금하네..? 설마 DB는 아닐꺼고 ..?)

### Q6-1. 환승 횟수 정의 (FC-13 3순위)
- A) 최단경로상 TRANSFER 엣지 통과 수 = 환승 횟수 ✅추천
- B) 노선 변경 횟수 별도 산정
- X) 기타

[Answer]: A 

### Q7. (미결4) 추천 < 15개 / 역별 편중
- A) 가능한 개수만 추천, 역별 최소보장 없음(점수순 그대로) ✅추천
- B) 역별 최소 N개 보장
- C) 최소 보장 개수 미만이면 반경 확대 재계산
- X) 기타

[Answer]: 이게 무슨 질문인지 이해가 안되는데 ?

### Q7-1. 추천 0개일 때 (FC-8 미결)
- A) 400 에러 + 안내 ✅추천
- B) 반경 확대 재시도
- X) 기타

[Answer]: B

---

## D. 추천 알고리즘 (FC-8)

### Q8. 스코어링 입력 항목
place: category_label, vibe[], occasion[], has_parking, reservable, rating 등. 모임: themeTagCode 보유.

- A) 모임 themeTag ↔ place vibe/occasion 매칭 + rating 가중 합산 점수로 정렬 ✅추천(단순 가중합)
- B) 카테고리 매칭만
- C) 상세 가중치는 설계 단계에서 확정
- X) 기타(가중치 직접 기술)

[Answer]: A 가 뭔소리하는거야? 어떻게 하겠다는건지 정확히 말해봐

### Q9. 역 귀속 태깅 기준 (FC-8 step5)
- A) 3개 역 중 최근접 역(PostGIS 거리)에 귀속 ✅추천
- B) 역 반경 내 포함 기준(중복 가능)
- X) 기타

[Answer]:이건 또 뭔말이야? 사용자들 중간 지점에 있는 역 3개를 뽑은뒤 거기서 Nkm(지금은 아마 2km로 고정해놨을텐데 이거는 조절 가능해야하는걸로 변경 필요) 에 해당하는 장소들 둥 하드필터에 해당하는 장소들중 특정 알고리즘(ex 사용자들-목적지 총 거리 * w1 +  옵션 or 분위기 태그 스코어 * w2 등 지금 내가 가지고 있는 정보들로 할 수 있는 제일 괜찮은 점수 매기기 알고리즘?) 으로 15개 정도 순위매겨 추천하는게 필요한데 무슨 3개역에서 뭔 가장 최근접 역이며 이런게 왜나와

### Q10. 카드의 "거리"(유저 출발지 기준, FC-9)
- A) 직선거리(PostGIS ST_Distance) ✅추천(가벼움)
- B) subway_edge 그래프 기반 거리
- X) 기타

[Answer]: 카드가 뭐야 ;;

---

## E. 투표 규칙 (FC-11/12)

### Q11. 투표 다중 제한
PRD: 1인 최대 = 후보수 50% 내림, 최소 1.

- A) PRD 그대로 ✅추천
- X) 기타

[Answer]: A

### Q12. 익명성 저장
- A) 투표 레코드에 member_id 저장하되 조회 시 집계/완료여부만 노출 ✅추천
- B) member_id 미저장(완전 익명)
- X) 기타

[Answer]: A

---

## F. 확장(Extensions) Opt-In

### Q13. Security Extension
보안 확장 규칙을 강제할까요?

- A) Yes — 모든 SECURITY 규칙 강제(프로덕션급 권장)
- B) No — SECURITY 규칙 생략(PoC/프로토타입)
- X) 기타

[Answer]: A

### Q14. Property-Based Testing Extension
- A) Yes — 모든 PBT 규칙 강제
- B) Partial — 순수함수/직렬화 라운드트립에만
- C) No — PBT 생략
- X) 기타

[Answer]: B

### Q15. TDD Code Generation Extension
- A) Yes — TDD 워크플로(토큰 1.5~2x, 결함 최소)
- B) No — 표준 코드 생성
- X) 기타

[Answer]: B
