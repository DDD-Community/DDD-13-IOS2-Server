# Requirements Clarification — Q1 & Q6 논의

---

## Q1 논의: meeting_participant 테이블 생성 (B안) 재검토

### 현재 고민
- B안(신규 테이블) 선호하지만 데이터 누적 + 로직 변경량이 걱정

### 분석

**B안(meeting_participant 신규 테이블)이 가져오는 변경 범위:**
1. `meeting_participant` 테이블 신규 생성 (meeting_id, member_id, location_point, attendance_status)
2. 모임 생성 시 그룹 멤버를 meeting_participant에 자동 복사하는 로직
3. 출석 변경 API → group_member 대신 meeting_participant 업데이트
4. `MeetingDetailService` 리팩토링 (group_member → meeting_participant 조회)
5. 데이터: meeting 수 × 멤버 수 만큼 레코드 누적

**A안(group_member + departure_place 사용)이 가져오는 변경 범위:**
1. `subway_station` 테이블 신규 생성만 (migration 1개)
2. `SubwayService` + 리포지토리 신규 생성
3. PostGIS 쿼리: `ST_SetSRID(ST_MakePoint(dp.longitude, dp.latitude), 4326)` 로 on-the-fly 포인트 구성
4. 기존 코드 변경 없음

### 핵심 질문
"이 모임에서만 다른 출발지로 설정" 기능이 **지금 또는 가까운 미래에 필요한가?**
- 필요하다면 B안이 맞음 (처음부터 설계)
- 지금은 불필요하다면 A안으로 시작 → 향후 B안으로 마이그레이션 가능

### 권장사항
A안으로 시작을 권장합니다. 이유:
- 지금 목표는 "중간지점 역 계산" 하나
- B안은 출석관리 전체 리팩토링을 수반 → MVP2 범위 과대
- A안도 도메인 서비스 인터페이스(`MidpointCalculator`)로 추상화하면 나중에 B안으로 교체해도 상위 레이어 변경 없음

## Clarification 1
meeting_participant 테이블 관련 최종 결정

A) A안 채택 — group_member + departure_place(default)로 중간지점 계산, 향후 마이그레이션 경로 유지
B) B안 채택 — 지금 제대로 설계, 출석관리 리팩토링 포함해서 진행
C) Other (please describe after [Answer]: tag below)

[Answer]: B로 가야겠다 A안으로가면 무조건 기본 출발지로만 해야하는거자나 이건 아니지

---

## Q6 논의: 역 후보 저장 & API 설계

### 현재 고민
- "메모리나 DB에 저장하고 API는 필요없다" vs "B처럼 해놔야 하나"

### 역 후보가 어떻게 쓰이는지 확인

전체 플로우:
```
중간지점 역 3개 확정
    ↓
이 역 근처 Nkm 장소 15개 뽑기 (테마/점수 기반)
    ↓
N일간 참여자들이 후보 등록
    ↓
투표 → 장소 선정
```

**클라이언트가 역 후보를 화면에 보여줘야 하나?**
- "우리 모임 중간지점은 홍대입구 / 합정 / 마포 근처입니다" 같은 UI가 있다면 API 필요
- 서버가 역 기반으로 장소만 뽑아서 클라이언트에는 장소 목록만 보여준다면 API 불필요

**저장 여부:**
- 온디맨드(매번 계산): 단순하지만 출발지 변경 시 역 후보도 바뀜 → 진행 중인 장소선정 세션과 충돌 가능
- 저장(location 단계 시작 시 1회): 역 후보가 고정됨 → 이후 장소 등록/투표의 기준점이 명확

### 권장사항
**상태 전이 시 1회 계산 + DB 저장**을 권장합니다.
이유: 장소 등록 단계가 N일간 진행되는 동안 역 후보가 바뀌면 혼란 → snapshot이 필요.
API는 단순 조회(저장된 값 반환)로 최소화.

## Clarification 2
역 후보 저장 및 API 방향 최종 결정

A) location 단계 시작 시 1회 계산 + `midpoint_station_candidate` 테이블 저장 + 조회 API
B) 온디맨드 계산 (매번 실시간) + 조회 API
C) 내부 계산만 (API 없음) — 서버가 역 후보 기반으로 장소 목록을 클라이언트에 바로 전달
D) Other (please describe after [Answer]: tag below)

[Answer]: A가 가장 괜찮아보이네 역 후보도 보여줘야할 수 있으니

---

답변 완료 후 "완료" 또는 "done"이라고 말씀해 주세요.
