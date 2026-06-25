# 요구사항 확인 질문 — FC-12 "현재 장소 참여중인 팀원" 조회 API

> 각 질문의 `[Answer]:` 뒤에 직접 답을 적어주세요. 객관식은 A/B/C 중 택1 또는 `X) 기타`로 자유 기술.
> 답변 완료 후 알려주시면 requirements 문서를 생성합니다.

## 요약 (현재 이해)
- **엔드포인트(신규)**: `GET /api/v1/meetings/{meetingId}/place-vote/participants` (읽기 전용)
- **검증**: 다른 PlaceVoteService 메서드처럼 모임 존재 + 호출자 그룹원 검증
- **응답**: 멤버 목록 — 각 멤버 `{ memberId, 이름(nickname), 프로필, 출발지명, 투표여부 }`

---

## Q1. 멤버 범위
어떤 팀원을 목록에 포함할까요?
- A) **활성 참여자(ABSENT 제외) 전원** — `getVoteStatus`/`getPlaceTravelBurden`와 동일 기준 (권장)
- B) ABSENT 포함 전체 참여자
- C) 그룹 전체 멤버

[Answer]: A

## Q2. 상태(LocationStatus) 검증 수준
"현재 장소 참여중" 의미상 투표 진행 상태를 강제할까요?
- A) **VOTING 상태 필수** — VOTING 아니면 에러(`PLACE_VOTE_NOT_IN_PROGRESS`). `getVoteStatus`와 동일
- B) 상태 무관 — 모임/그룹원 검증만, 세션 없으면 투표여부는 모두 false
- C) VOTING 또는 CONFIRMED 허용

[Answer]: A

## Q3. 투표여부(voted) 판정 기준
"투표했는지"는 무엇으로 판단할까요?
- A) **현재 진행 세션에 1표 이상 제출한 멤버 = true** (distinct voter). 기존 `getVoteStatus.memberStatuses`와 동일 정의 (권장)
- B) 기타

[Answer]: A

## Q4. 출발지(departureName) 표기
어떤 값을 "출발지"로 노출할까요?
- A) **기존 `resolveDepartureName` 로직 재사용** — 참여자 좌표와 일치하는 출발지명(placeName→label), 없으면 기본 출발지, 그래도 없으면 null (`getPlaceTravelBurden`와 동일) (권장)
- B) 출발지명 + 좌표(위/경도)까지 포함
- C) 기타

[Answer]: A

## Q5. 프로필(profile) 노출 형태
프로필 이미지는 어떤 형태로 줄까요?
- A) **`profileImageUrl` 원본 object key 그대로** — 기존 `MemberPickStatus` 컨벤션과 동일(클라이언트가 resolve) (권장, 일관성)
- B) `storageService.generateSignedReadUrl(...)`로 변환한 **서명 URL** (멤버 프로필 단건 API와 동일)
- C) 기타

[Answer]: A

## Q6. 응답에 본인 표시(isMe) 포함 여부
호출자 자신을 구분하는 `isMe` 플래그를 넣을까요? (`getPlaceTravelBurden`에는 있음)
- A) 포함
- B) 미포함

[Answer]: A

## Q7. 추가 메타데이터
목록과 함께 줄 요약 정보가 더 필요한가요? (예: 활성 인원수 totalActive, 투표완료 인원수 votedCount)
- A) 멤버 목록만
- B) **totalActive + votedCount 요약 포함** (`getVoteStatus`와 유사)
- C) 기타

[Answer]: A
