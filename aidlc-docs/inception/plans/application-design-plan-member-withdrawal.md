# Application Design 계획 — 회원 탈퇴 기능

- 작성일: 2026-08-24
- 입력: `requirements-member-withdrawal.md`, `execution-plan-member-withdrawal.md`

---

## 1. 설계 작업 체크리스트

- [x] 컴포넌트 정의 (신규/변경 컴포넌트와 책임)
- [x] 컴포넌트 메서드 시그니처 정의
- [x] 서비스 레이어 오케스트레이션 설계 (트랜잭션 경계 포함)
- [x] 컴포넌트 의존 관계 및 통신 패턴 정의
- [x] 파기 매트릭스를 리포지토리 메서드로 매핑
- [x] Apple revoke 클라이언트 인터페이스 설계 (미설정 시 skip 경로 포함)
- [x] 설계 일관성 검증

---

## 2. 설계 확인 질문 (3건)

> 각 항목에 권장안을 적어두었습니다. 이견 없으면 `권장` 한 단어로 답해주셔도 됩니다.

---

### D1. `appleAuthorizationCode` 전달 방식

Q11에서 `DELETE /api/v1/members/me` 로 확정했는데, DELETE는 요청 바디를 싣는 것이 표준적이지 않습니다. 전달 방식을 정해야 합니다.

- A) **요청 바디** — `@RequestBody(required = false)`. Spring·OkHttp·URLSession 모두 지원하나 일부 프록시가 DELETE 바디를 누락시킬 수 있음
- B) **쿼리 파라미터** — `DELETE /api/v1/members/me?appleAuthorizationCode=xxx`. 구현 단순하나 **액세스 로그에 코드가 평문으로 남음**
- C) **커스텀 헤더** — `X-Apple-Authorization-Code`. 로그 노출 없고 프록시 안전. 다소 비관용적
- D) **`POST /api/v1/members/me/withdraw` 로 변경** — 바디 사용이 자연스러워짐. 단 Q11 결정을 뒤집음
- X) 기타

**권장: C (커스텀 헤더)**
authorization code는 단기·일회용이지만 인증 자격에 준하므로 URL(로그 잔존)을 피하는 편이 낫고, DELETE 결정을 유지하면서 프록시 리스크도 없습니다.

[Answer]: 권장 (C — 커스텀 헤더 X-Apple-Authorization-Code)

---

### D2. 탈퇴 오케스트레이션의 위치

탈퇴는 `auth`(회원·토큰), `member`(출발지·약관), `group`(호스트 승계), `meeting`(참여·이동부담), `storage`(이미지) 5개 컨텍스트를 건드립니다.

- A) **기존 `MemberService` 에 `withdraw()` 추가** — 파일 추가 없음. 단 `MemberService` 가 회원가입+탈퇴+프로필까지 떠안아 비대해짐
- B) **신규 `MemberWithdrawalService` 분리** (`member/application/`) — 탈퇴 단일 책임. 클래스 1개 증가
- C) `auth` 컨텍스트에 배치 — `Member` 도메인 소유 컨텍스트가 `auth` 이므로
- X) 기타

**권장: B (신규 `MemberWithdrawalService`)**
5개 컨텍스트를 조율하는 유스케이스라 단일 책임으로 떼어내는 편이 낫고, 파기 순서·트랜잭션 경계가 한 파일에서 읽힙니다. `MemberService` 는 이미 회원가입 오케스트레이션을 담당 중이라 성격도 다릅니다.

[Answer]: 권장 (B — 신규 MemberWithdrawalService)

---

### D3. 크로스 컨텍스트 데이터 파기 호출 방식

`MemberWithdrawalService` 가 타 컨텍스트 데이터를 지우는 방법입니다.

- A) **리포지토리 직접 주입** — 5개 컨텍스트의 Repository를 서비스에 주입해 순차 호출. 단순·명시적, 순서 제어 쉬움. 컨텍스트 간 결합 발생
- B) **도메인 이벤트 발행** — `MemberWithdrawn` 이벤트를 각 컨텍스트가 구독해 자기 데이터 삭제. 결합도 낮음. 단 파기 누락 시 추적 어렵고 트랜잭션 경계가 복잡해짐
- C) 각 컨텍스트에 파기 전용 서비스를 두고 그것만 호출 (A와 B의 절충)
- X) 기타

**권장: A (리포지토리 직접 주입)**
DDD 관점에서는 B가 이상적이지만, **개인정보 파기는 누락 시 법적 리스크가 있어 "무엇이 지워지는지 한눈에 보이고 실패 시 전체 롤백되는" 명시적 순차 호출이 안전**합니다. 이벤트 기반은 비동기·부분 실패 시 파기 누락을 추적하기 어렵습니다. 규모도 5개 호출 수준이라 결합 비용이 크지 않습니다.

[Answer]: 권장 (A — 리포지토리 직접 주입)

---

## 3. 산출물

승인 후 아래를 생성합니다.

- `aidlc-docs/inception/application-design/application-design-member-withdrawal.md`
  (컴포넌트 / 메서드 시그니처 / 서비스 오케스트레이션 / 의존관계 통합)
