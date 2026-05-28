# Bangawo 전체 플로우 개요

---

## 사용자 여정

```mermaid
flowchart TD
    A([앱 최초 진입]) --> B[소셜 로그인]
    B --> C[회원가입\n닉네임 + 약관 동의 + 출발지 등록]

    C --> D{그룹 있나?}
    D -- 없음\n호스트 --> E[그룹 & 첫 모임 생성\nmeeting_participant 자동 생성]
    D -- 초대링크 받음\n구성원 --> F[초대 코드 입력 → 합류\nmeeting_participant 자동 생성]

    E --> G[모임 상세 진입]
    F --> G

    G --> H{날짜 정하기}
    H -- 방식 A --> I[호스트 단독 선택\n즉시 확정]
    H -- 방식 B --> J[투표 시작\n구성원 투표]
    J --> K{마감 or 전원 투표}
    K -- 1위 확정 --> L[날짜 확정\ndateVoteStatus = COMPLETED]
    K -- 동률/투표자 없음 --> H
    I --> L

    L --> M[출발지 미설정 구성원은\n이 시점에 출발지 등록/변경]
    M --> N[호스트가 장소 정하기 시작\nPOST /location/start]
    N --> O[중간지점 역 후보 3개 계산\nPostGIS 중심점 기준]
    O --> P[구성원 역 후보 조회\nlocationStatus = IN_PROGRESS]

    P --> Q{모임 날짜 지남?}
    Q -- 스케줄러 자동 종료 --> R[모임 종료\nMeetingStatus = CLOSED]
    R --> S{새 모임?}
    S -- 호스트가 시작 --> E
    S -- 그룹 종료 --> T([종료])
```

---

## 상태 전이

```mermaid
stateDiagram-v2
    direction LR

    state "MeetingStatus" as MS {
        ACTIVE --> CLOSED : 스케줄러\nconfirmedDate 지남
    }

    state "DateVoteStatus" as DVS {
        BEFORE --> IN_PROGRESS : 투표 시작
        BEFORE --> COMPLETED : host-pick
        IN_PROGRESS --> COMPLETED : 호스트 confirm\n/ 스케줄러 1위
        IN_PROGRESS --> BEFORE : 스케줄러\n동률·투표자 없음 (리셋)
    }

    state "LocationStatus" as LS {
        [*] --> BEFORE2
        BEFORE2 --> IN_PROGRESS2 : POST /location/start\n(HOST)
        BEFORE2 : BEFORE
        IN_PROGRESS2 : IN_PROGRESS
    }
```

---

## FC별 기능 요약

| FC | 기능 | 주요 API | 관련 테이블 |
|---|---|---|---|
| FC-4 | 그룹 & 모임 생성 | `POST /groups/create` | group_info, meeting, group_member, meeting_participant |
| FC-5 | 초대 & 합류 | `POST /groups/{id}/invite`<br>`POST /groups/join` | group_invite, group_member, meeting_participant |
| FC-6 | 모임 리스트 (홈) | `GET /meetings` | meeting, group_member, departure_place |
| FC-7 | 날짜 투표 | `POST /date-vote`<br>`POST /date-vote/host-pick`<br>`POST /date-vote/submit`<br>`PATCH /date-vote/confirm` | date_vote_session, date_vote_option, date_vote_record |
| FC-7-1 | 내 정보 수정 | `PATCH /groups/{id}/members/me/attendance`<br>`POST /departure-places`<br>`PUT /departure-places/{id}`<br>`PATCH /meetings/{id}/participants/me/departure` | group_member, departure_place, meeting_participant |
| FC-8 | 그룹 생명주기 | `PATCH /groups/{id}/close`<br>`POST /groups/{id}/meetings` | group_info, meeting |
| FC-midpoint | 중간지점 역 추천 | `POST /meetings/{id}/location/start`<br>`GET /meetings/{id}/midpoint-stations` | meeting_participant, subway_station, midpoint_station_candidate |

---

## 테이블 생성 시점

| 테이블 | 언제 레코드가 생기나 |
|---|---|
| `member` | 소셜 로그인 최초 진입 시 |
| `group_info` | 호스트가 그룹 생성 시 |
| `meeting` | 그룹 생성 시 (첫 모임) / 새 모임 생성 시 |
| `group_member` | 그룹 생성 시 (호스트) / 초대 합류 시 (구성원) |
| `group_invite` | 호스트가 초대 코드 발급 시 (기존 코드 삭제 후 재생성) |
| `meeting_participant` | 그룹 생성 시 (호스트) / 초대 합류 시 (구성원) |
| `departure_place` | 회원가입 시 / 출발지 추가 시 |
| `date_vote_session` | 호스트가 날짜 투표 시작 시 |
| `date_vote_option` | 날짜 투표 시작 시 (후보 날짜 수만큼) |
| `date_vote_record` | 구성원이 투표 시 |
| `midpoint_station_candidate` | `POST /location/start` 호출 시 |

---

## 권한 정리

| 액션 | HOST | MEMBER |
|---|---|---|
| 그룹 생성 | O | — |
| 초대 코드 발급 | O | X |
| 날짜 투표 시작 | O | X |
| 날짜 직접 확정 | O | X |
| 투표 참여 | O | O |
| 참석여부 변경 | O (본인) | O (본인) |
| 출발지 추가/수정 | O | O |
| 모임 출발지 변경 | O | O |
| 장소 선정 시작 | O | X |
| 역 후보 조회 | O | O |
| 그룹 종료 | O | X |
| 새 모임 생성 | O | X |
