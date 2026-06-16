# Unit of Work — FC/요구사항 매핑 (FC-8~13)

> User Stories는 SKIP(백엔드 API). PRD FC + requirements FR을 유닛에 매핑.

| Unit | FC / FR | 산출 핵심 | API |
|---|---|---|---|
| U1 기반 | 상태모델, FR-8.1 가드, 모임입력확장 | LocationStatus, Meeting 가드, ErrorCode, V18/V26 | `POST /meetings`(입력확장) |
| U2 추천 | FC-8 / FR-8.2~8.8 | place 컨텍스트, 추천 스코어링, V19/V20 | `POST /meetings/{id}/location/start`, `GET /meetings/{id}/recommendations`, `GET /places/options` |
| U3 담기 | FC-9 / FR-9.1~9.5 | pick, 담기현황, 전환, 담기마감배치, V21 | `GET /meetings/{id}/places`, `POST/DELETE /meetings/{id}/places/{placeId}/pick`, `GET /meetings/{id}/places/pick-status`, `POST /meetings/{id}/place-vote`(호스트 시작) |
| U4 투표 | FC-11·12 / FR-11·12 | 그래프, 투표세션/투표/이동부담, V22~V24 | `POST /meetings/{id}/place-vote/submit`, `GET /meetings/{id}/place-vote` |
| U5 확정 | FC-13 / FR-13 | 4단계 순위, 확정저장, V25 | `GET /meetings/{id}/place-result` |

## 커버리지 확인
- FR-8 → U1(가드)+U2(추천) ✔
- FR-9 → U3 ✔
- FR-11/12 → U4 ✔
- FR-13 → U5 ✔
- NFR(그래프 부팅·스냅샷·스케줄러) → U2/U4 ✔
- 모든 FR이 유닛에 할당됨.

## 알림
- 범위 제외(푸시·인앱) — 유닛 없음. 상태전환 트리거 지점만 주석으로 표기.
