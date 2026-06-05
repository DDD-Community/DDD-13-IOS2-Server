# Requirements Clarification — 중간지점 역 후보 추출 (MVP2)

## 사전 분석 요약

현재 코드베이스 분석 결과:
- `departure_place` 테이블: latitude/longitude (DOUBLE, PostGIS geometry 아님)
- `subway_station` 테이블: **미존재** → 신규 생성 필요
- `meeting_participant` 테이블: **미존재** → group_member + departure_place 활용 예정
- 제공된 SQL 쿼리의 `meeting_participant.location_point`는 DB에 없는 구조

---

## Question 1
출발지 기하학적 중심(centroid) 계산 시 참여자 데이터 소스를 어떻게 할까요?

현재 `departure_place`는 회원 개인 기준으로 저장되어 있고, 모임별 출발지는 별도 테이블이 없습니다.

A) group_member(attendance_status != ABSENT) + departure_place(is_default=true) 사용 — 기존 데이터 활용, 추가 테이블 불필요
B) 새 `meeting_participant` 테이블 생성 — 모임별 출발지 좌표 별도 저장, 향후 "이 모임만 다른 출발지 선택" 가능
C) Other (please describe after [Answer]: tag below)

[Answer]: B로 하는게 깔끔할것 같은데 이렇게 하면 모임별 참가자들 데이터가 너무 많이 쌓일까봐 걱정이긴해 로직도 많은부분 변경이 있어야하고 이부분은 상의해줄꺼지?

---

## Question 2
`departure_place`가 없는 멤버가 있을 경우 처리 방식은?

A) 해당 멤버 제외하고 나머지 출발지로 중심 계산 (조용히 스킵)
B) API 호출 시 에러 반환 — 출발지 미등록 멤버가 있으면 계산 불가
C) 출발지 없는 멤버는 제외하되, Response에 "출발지 미등록 멤버 수" 포함
D) Other (please describe after [Answer]: tag below)

[Answer]: D 회원가입할때 기본 출발지는 필수 입력이라 없지는 않을텐데 없다고하면 에러로 하자

---

## Question 3
역 후보 정렬 기준은 무엇인가요?

제공된 SQL은 `dist_m ASC` (중심에서 가까운 순)로 되어 있고 score_drink 점수도 있습니다.

A) 거리순 (dist_m ASC) — 가장 가까운 역 우선
B) 점수순 (score_drink DESC) — 술/식당 점수 높은 역 우선
C) 복합 (거리 + 점수 가중치) — 거리와 점수를 조합
D) Other (please describe after [Answer]: tag below)

[Answer]: D 사실 역마다 스코어를 넣으려고했는데 지금은 일단 거리순으로 해야할것 같아

---

## Question 4
subway_station 테이블에 어떤 점수 컬럼이 필요한가요?

이후 장소 후보 등록 시 테마(술, 밥, 카페 등)별 점수가 필요하다고 하셨습니다.

A) score_drink 하나만 (이번에 우선 구현, 나중에 추가)
B) score_drink, score_food, score_cafe 3개 (주요 카테고리 미리 정의)
C) 일단 점수 컬럼 없이 거리 기반만, 추후 ALTER TABLE로 추가
D) Other (please describe after [Answer]: tag below)

[Answer]: C 우선 거리기반으로만 하자

---

## Question 5
subway_station 데이터는 어떻게 로드할 계획인가요?

공공데이터 역사_ID/역사명/호선/위도/경도 보유 중이라고 하셨습니다.

A) Flyway V11 SQL 마이그레이션으로 INSERT (CSV → SQL 변환하여 포함)
B) 별도 데이터 로더 스크립트 (psql 또는 Spring 배치로 직접 import)
C) Other (please describe after [Answer]: tag below)

[Answer]: C 내가 일단 준 헤더와 데이터가 있으니 너가 테이블 만들어주면 내가 따로 넣을께

---

## Question 6
API 엔드포인트 동작 방식은?

A) `GET /meetings/{meetingId}/midpoint-stations` — 요청 시 즉시 계산 반환
B) 모임 상태 전이 시 자동 계산 후 DB 저장 → API는 저장값 조회만
C) Other (please describe after [Answer]: tag below)

[Answer]: C 내가 생각한건 우선 장소후보 추려주기전 필요한 중요 데이터라 메모리나 DB에 따로 저장하고 API는 필요없다 생각했는데 간단하게 B처럼 해놔야하나

---

## Question 7
반환할 역 후보 개수는?

A) 고정 3개
B) 파라미터로 받음 (기본 3, 최대 N)
C) Other (please describe after [Answer]: tag below)

[Answer]: A API로 가져올 데이터가 아니긴해서 일단은 서비스에 고정으로 3개 해놔야할것 같아

---

답변 완료 후 "완료" 또는 "done"이라고 말씀해 주세요.
