# FC-7 처리 흐름

---

## 1. 모임 상세 조회

`GET /api/v1/meetings/{meetingId}`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. groupMember 조회 → 호출자가 이 그룹 소속인지 확인 (없으면 403)
4. 같은 그룹의 groupMember 전체 조회 (joined_at ASC)
5. member 정보 조회 (nickname, profileImageUrl)
6. departure_place 조회 (멤버별 전체 목록)
7. themeTag → displayName 조회
8. MeetingDetailResponse 조립 후 반환

---

## 2. 방식 A — 호스트 단독 날짜 선택

`POST /api/v1/meetings/{meetingId}/date-vote/host-pick`  
body: `{ "date": "2026-06-15" }`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. groupMember 조회 → HOST 확인 (아니면 403)
4. `meeting.dateVoteStatus == BEFORE` 확인 (아니면 400)
5. `date > 오늘` 확인 (아니면 400)
6. `meeting.confirmedDate = date`
7. `meeting.dateVoteStatus = COMPLETED` ← 즉시 확정
8. meeting 저장

> 알림 없음 (PRD 명시)

---

## 3. 방식 B — 투표 시작

`POST /api/v1/meetings/{meetingId}/date-vote`  
body: `{ "candidateDates": ["2026-06-10", "2026-06-14", "2026-06-17"], "durationDays": 3 }`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. groupMember 조회 → HOST 확인 (아니면 403)
4. `meeting.dateVoteStatus == BEFORE` 확인 (아니면 400)
5. candidateDates 검증 — 1~10개, 오늘 이후, 중복 없음
6. durationDays 검증 — 1 / 3 / 7 중 하나
7. date_vote_session 생성 (`method=VOTE`, `deadline=오늘+durationDays`, `status=ACTIVE`)
8. date_vote_option 생성 (후보 날짜 수만큼, sort_order = 입력 순서)
9. `meeting.dateVoteStatus = IN_PROGRESS`
10. meeting / session / options 저장

> FCM 알림 → MVP1 no-op

---

## 4. 투표 참여

`POST /api/v1/meetings/{meetingId}/date-vote/submit`  
body: `{ "optionIds": [1, 3] }`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. ⭐ **meeting_participant 조회 → 이 모임 참여자 확인 (아니면 403 MEETING_024)** — 그룹원 검증 아님
4. `meeting.dateVoteStatus == IN_PROGRESS` 확인 (아니면 400)
5. session 조회 → `deadline >= 오늘` 확인 (마감이면 400)
6. optionIds가 이 session 소속인지 확인 (아니면 400)
7. 이 멤버의 기존 투표 기록 전부 삭제 (재투표 허용)
8. date_vote_record 저장 (optionId별, DB UNIQUE(option_id, member_id)로 중복 방지)

**전원 투표 조기 종료 체크** ⭐ (2026-07-05 분모 수정)

9. **이 모임의 참여자 수(N) 조회** (`meeting_participant` 기준) — ~~기존: group_member 전체 수~~
10. 이번 session에서 투표한 distinct 멤버 수(M) 조회
11. `N == M` 이면 스케줄러 동일 로직 즉시 실행 → [5번 스케줄러 Case A/B/C 참고]

> 기존엔 분모를 그룹원 전체 수로 잡아, 모임이 그룹원 일부만 참여자로 가지면 `N > M`이 유지돼 조기 확정이 트리거되지 않았다. 분모를 모임 참여자 수로 교정.

---

## 5. 투표 현황 조회

`GET /api/v1/meetings/{meetingId}/date-vote`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. ⭐ meeting_participant 조회 → 이 모임 참여자 확인 (아니면 403 MEETING_024)
4. session 조회 (없으면 빈 options 반환)
5. option 목록 조회
6. record 목록 조회 (option_id IN 목록)
7. option별 집계
   - `voteCount` = 이 option에 투표한 record 수
   - `voters` = 투표자 member 정보
   - `isMyVote` = 내 memberId 기록 있으면 true
8. 정렬: `voteCount DESC` → `sort_order ASC`
9. VoteStatusResponse 반환

---

## 6. 호스트 수동 확정

`PATCH /api/v1/meetings/{meetingId}/date-vote/confirm`  
body: `{ "optionId": 2 }`

1. JWT → memberId 추출
2. meeting 조회 (없으면 404)
3. groupMember 조회 → HOST 확인 (아니면 403)
4. `meeting.dateVoteStatus == IN_PROGRESS` 확인 (아니면 400)
5. option 조회 → 이 session 소속인지 확인 (아니면 400)
6. `meeting.confirmedDate = option.candidateDate`
7. `meeting.dateVoteStatus = COMPLETED`
8. `session.status = CONFIRMED`
9. meeting / session 저장

> FCM 알림 → MVP1 no-op

---

## 7. 스케줄러 — 매일 자정 자동 처리

**실행 환경**

| 항목 | 값 |
|---|---|
| cron | `0 0 0 * * *` (매일 00:00 KST) |
| timezone | `Asia/Seoul` |
| 진입점 | `MeetingScheduler` (@Scheduled, @Transactional 없음) |
| 트랜잭션 | 건별 독립 — `VoteSchedulerService` / `MeetingSchedulerService`에 위임, 한 건 실패해도 다른 건 영향 없음 |

**Step 1 — 마감된 투표 세션 조회**

```sql
SELECT * FROM date_vote_session
WHERE status = 'ACTIVE' AND deadline < 오늘
```

**Step 2 — 각 세션 처리 (`VoteSchedulerService.processExpiredSession`)**

| 케이스 | 조건 | 처리 |
|---|---|---|
| A: 명확한 1위 | 최다 득표 option이 1개 | `confirmedDate` 설정, `dateVoteStatus = COMPLETED`, `session.status = CONFIRMED` |
| B: 투표자 없음 | total count == 0 | `session.status = EXPIRED`, `dateVoteStatus = BEFORE` (호스트 재선택 가능) |
| C: 동률 | 최다 득표 option이 2개 이상 | `session.status = EXPIRED`, `dateVoteStatus = BEFORE` (호스트 결정 필요) |

> 실패 시 log.error 후 다음 세션으로 계속 진행

**Step 3 — 자동 종료 대상 조회**

```sql
SELECT * FROM meeting
WHERE status = 'ACTIVE' AND confirmed_date < 오늘
```

**Step 4 — 각 모임 종료 (`MeetingSchedulerService.closeMeeting`)**

- `meeting.status = CLOSED` 저장
- 실패 시 log.error 후 다음 모임으로 계속 진행

---

## 8. 새 모임 생성 (참여자 명단 선택)

`POST /api/v1/groups/{groupId}/meetings`  
body: `{ "name": "2차 회식", "themeTagCode": "DINING", "participantMemberIds": [12, 34] }`

1. JWT → memberId 추출
2. groupMember 조회 → HOST 확인 (아니면 403)
3. 해당 그룹의 최신 meeting 조회
4. `meeting.status == CLOSED` 확인 (아니면 400 — 현재 모임 진행 중)
5. 현재 그룹 구성원 id 집합 조회
6. 참여자 집합 구성: **호스트 자동 포함** + `participantMemberIds` (중복 제거)
7. 참여자가 모두 현재 그룹 구성원인지 검증 (아니면 403 NOT_GROUP_MEMBER)
8. 새 meeting 생성 (`status=ACTIVE`, `dateVoteStatus=BEFORE`, `locationStatus=BEFORE`)
9. **선택된 참여자 각각 `meeting_participant` 시딩** (attendance=JOIN, 각자 default 출발지 좌표; 없으면 null)
10. meetingId 반환

> 핵심: 2번째 이후 모임은 **그룹 전원이 아니라 호스트가 고른 명단**만 참여자로 들어간다.  
> 첫 모임(`POST /groups/create`)은 그 시점 구성원이 호스트뿐이라 호스트만 시딩됨.  
> 합류(`POST /groups/join`)는 가입 시점의 열린 미팅에 자동 참여(현행 유지).

### 참여자 선택용 — 그룹 구성원 목록 조회

`GET /api/v1/groups/{groupId}/members`

1. JWT → memberId 추출
2. groupMember 조회 → 구성원 확인 (아니면 403 NOT_GROUP_MEMBER)
3. 그룹 구성원 목록 반환 (joinedAt 오름차순)
   - `{ memberId, nickname, profileImageUrl, role, joinedAt }`
   - 탈퇴 회원은 nickname/profileImageUrl null

---

## 상태 전이

**MeetingStatus**

```
ACTIVE ──[스케줄러: confirmedDate 지남]──→ CLOSED
```

- `CLOSED` 여야 같은 그룹에서 새 모임 생성 가능

**DateVoteStatus**

```
BEFORE ──[host-pick]────────────────────────────────→ COMPLETED
BEFORE ──[vote 시작]────────────────────────────────→ IN_PROGRESS
IN_PROGRESS ──[호스트 confirm]──────────────────────→ COMPLETED
IN_PROGRESS ──[스케줄러: 명확한 1위]────────────────→ COMPLETED
IN_PROGRESS ──[스케줄러: 투표자 없음 / 동률]─────────→ BEFORE (리셋)
```

- `BEFORE` 리셋 = 호스트가 host-pick 또는 새 투표를 다시 시작 가능
