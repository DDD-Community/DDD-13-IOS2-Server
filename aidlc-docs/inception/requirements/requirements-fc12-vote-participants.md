# 요구사항 — FC-12 보완: 출발지 메타 저장 + "현재 장소투표 참여중인 팀원 조회" API

## Intent 분석
- **User Request**: (1) `meeting_participant`에 출발지 좌표뿐 아니라 **출발지 이름 메타(label·placeName·address)를 직접 저장**하도록 스키마/쓰기경로 수정. (2) 그 저장값을 사용해 **현재 장소투표에 참여중인 팀원 목록 조회 API** 신규 추가.
- **Request Type**: Enhancement(스키마 보강) + New Feature(read-only 조회 API)
- **Scope**: Multiple Components — `meeting`(participant 도메인/엔티티/서비스), `group`(생성 쓰기경로 2곳), `meeting`(거리보기 읽기경로 리팩터), Flyway V30
- **Complexity**: Moderate (스키마 변경 + 다중 쓰기경로 + 마이그레이션 백필 + 신규 API)
- **FC 매핑**: 기존 **FC-12 확장** (새 폴더 금지, `fc12/` 갱신 + `project-erd.md` 갱신)

## 배경 / 문제
- 현재 `meeting_participant`는 출발 **좌표(lat/lng)만** 저장. 출발지 "이름"이 없다.
- 이름이 필요할 때마다 `meeting_participant.좌표 ↔ departure_place.좌표`를 `1e-6` 오차로 역매칭(`resolveDepartureName`)해서 뽑는 구조 → 취약(저장 출발지 삭제/수정 시 매칭 실패), 매 조회마다 `departure_place` 추가 조회 필요.
- **결정**: 참여자가 출발지를 고르는 **쓰기 시점에 이름 메타를 스냅샷으로 함께 저장**하고, 읽기 시점엔 그 값을 그대로 사용한다.

---

## 기능 요구사항

### FR-1. meeting_participant 출발지 메타 컬럼 추가 (V30)
- 추가 컬럼 (모두 nullable — 출발지 미등록 참여자 허용):
  - `departure_label` VARCHAR(100) — 사용자 별칭(예: "집", "회사")
  - `departure_place_name` VARCHAR(150) — 카카오 장소명(nullable)
  - `departure_address` VARCHAR(255) — 주소(도로명 우선, 없으면 지번)
- 기존 `latitude`/`longitude`/`attendance_status`는 유지.
- **백필**: 기존 행은 `member_id`의 **기본 출발지(is_default)** 기준으로 best-effort 채움. 매칭 불가 행은 null 유지(읽기 시 null 노출).

### FR-2. 출발지 쓰기 경로 3곳 메타 동시 저장
조회한 `DeparturePlace`에서 label/placeName/address를 꺼내 `MeetingParticipant`에 함께 저장:
1. `GroupService.createGroup` — 호스트 참여자 생성(기본 출발지)
2. `GroupInviteService.createMeetingParticipant` — 초대 합류 참여자 생성(기본 출발지)
3. `PlaceSelectionService.updateParticipantDeparture` — 참여자가 출발지 변경(선택 출발지)

### FR-3. 도메인/엔티티 변경
- `MeetingParticipant`: 필드 `departureLabel`, `departurePlaceName`, `departureAddress` 추가.
  - `create(...)`/`updateDeparture(...)` 시그니처에 메타 3종 추가.
  - 표시명 헬퍼 `departureName()` = `placeName != null ? placeName : label` (둘 다 없으면 null).
- `MeetingParticipantJpaEntity`: 컬럼 매핑 + `from`/`toDomain`/`updateCoords`(→`updateDeparture`) 반영.

### FR-4. 거리보기(getPlaceTravelBurden) 읽기 리팩터
- `resolveDepartureName`(좌표 역매칭) 제거 → `participant.departureName()` 직접 사용.
- `departurePlaceRepository` 조회/매칭 로직 제거(불필요해짐).
- 응답 `departureName` 값 동작은 동일/개선(스냅샷 기반).

### FR-5. 신규 API — 현재 장소투표 참여중인 팀원 조회
- **Endpoint**: `GET /api/v1/meetings/{meetingId}/place-vote/participants`
- **인증**: 로그인 필요(Authentication → memberId)
- **검증**(기존 PlaceVoteService 패턴):
  - 모임 존재(`MEETING_NOT_FOUND`)
  - 호출자 그룹원(`NOT_GROUP_MEMBER`)
  - LocationStatus == VOTING (`PLACE_VOTE_NOT_IN_PROGRESS`) — *Q2=A*
- **멤버 범위**: 활성 참여자(ABSENT 제외) 전원 — *Q1=A*
- **응답 항목(멤버별)**:
  - `memberId`
  - `name` (회원 nickname)
  - `profileImageUrl` (원본 object key, 클라이언트 resolve — *Q5=A*)
  - `departureName` (저장된 메타, placeName→label, 없으면 null) — *Q4: 저장값 사용으로 변경*
  - `isMe` (호출자 본인 여부 — *Q6=A*)
  - `voted` (현재 세션에 1표+ 제출 = true — *Q3=A*)
- **정렬**: 미정 → 기본 참여 등록순(participant 조회순) 유지.

---

## 비기능 요구사항
- **성능**: 조회 1회당 쿼리 = 모임/그룹원/세션/참여자/표/회원 배치 — N+1 없음(`findAllById`, `findBySessionId` 배치).
- **일관성**: 출발지 표기 = "쓰기 시점 스냅샷". 회원이 나중에 저장 출발지를 바꿔도 모임 참여 당시 이름 유지(의도된 동작).
- **하위호환**: 신규 컬럼 nullable + 백필 → 기존 데이터/기존 API 무영향.
- **DDD**: domain은 JPA 비의존 유지, 비즈니스 헬퍼(`departureName()`)는 도메인에 둔다.

## 스코프 외 (Out of Scope)
- 출발지 메타의 실시간 동기화(저장 출발지 수정 시 과거 참여 행 갱신) — 안 함.
- 좌표 자체 정합성 검증/지오코딩 — 안 함.

## 확정된 결정사항 (사용자 합의)
1. 출발지 이름은 **meeting_participant에 직접 저장**(좌표 역매칭 폐기). label·placeName·address 함께 저장.
2. 쓰기 3경로 모두 DeparturePlace에서 메타 추출해 저장.
3. 신규 조회 API는 저장된 메타 사용. VOTING 필수, 활성 참여자 전원, 멤버별 voted 포함.
4. 기존 행은 기본 출발지 기준 best-effort 백필(V30), 매칭 불가 시 null.

## RA 답변 반영 (이전 질문지)
- Q1=A 활성참여자 전원 / Q2=A VOTING필수 / Q3=A 세션 1표+ / Q4=저장메타사용 / Q5=A 원본key / Q6=A isMe포함 / Q7=A 멤버목록(요약 totalActive/votedCount는 옵션, 기본 미포함)
