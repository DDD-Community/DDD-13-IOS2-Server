# API Documentation (RE refresh — FC-8~13 관점, 2026-06-16)

## 공통 규칙
- Base URL: `/api/v1`
- 인증: `Authorization: Bearer {accessToken}` (공개 엔드포인트 제외)
- 에러 형식: `{ "code": "MEETING_001", "message": "..." }`

## 장소 선정 관련 기존 엔드포인트 (LocationController)
| Method | Path | 설명 |
|---|---|---|
| POST | `/meetings/{meetingId}/location/start` | 장소 선정 단계 시작 — 호스트, 중간지점 역 3개 자동 계산·저장 |
| PATCH | `/meetings/{meetingId}/participants/me/departure` | 내 모임 출발지 변경 |
| GET | `/meetings/{meetingId}/midpoint-stations` | 중간지점 역 후보 rank 1~3 조회 |

## 모임 관련 기존 엔드포인트 (MeetingController)
| Method | Path | 설명 |
|---|---|---|
| GET | `/meetings` | 내 모임 리스트 |
| GET | `/meetings/{meetingId}` | 모임 상세 |
| POST | `/meetings/{meetingId}/date-vote/host-pick` | 날짜 호스트 단독 확정 |
| POST | `/meetings/{meetingId}/date-vote` | 날짜 투표 시작 |
| POST | `/meetings/{meetingId}/date-vote/submit` | 날짜 투표 참여 |
| GET | `/meetings/{meetingId}/date-vote` | 날짜 투표 현황 |
| PATCH | `/meetings/{meetingId}/date-vote/confirm` | 호스트 수동 날짜 확정 |

## FC-8~13 신규 엔드포인트 (예상 — RA/설계에서 확정)
- FC-8: `POST /location/start` 확장 (추천 15개 산출 포함) / `GET .../recommendations`
- FC-9: `GET .../places`(역·카테고리·필터), `POST/DELETE .../places/{placeId}/pick`(담기/취소), `GET .../places/pick-status`
- FC-11/12: `POST .../place-vote`(생성+마감일), `POST .../place-vote/submit`, `GET .../place-vote`(현황+이동부담)
- FC-13: 자동 확정(스케줄러/전원완료 트리거) — `GET .../place-result`

## 기존 ErrorCode (발췌 — FC-8~13 관련)
| 코드 | HTTP | 의미 |
|---|---|---|
| GROUP_003 | 403 | 그룹 구성원 아님 |
| GROUP_004 | 403 | 호스트만 수행 가능 |
| MEETING_001 | 404 | 모임 없음 |
| MEETING_010 | 400 | 장소 선정 이미 시작됨 |
| MEETING_011 | 400 | 출발지 미등록 참여자 존재 |
| MEETING_012 | 400 | 중간지점 근처 역 없음 |
| MEETING_013 | 404 | 모임 참여자 없음 |
→ FC-9/11/12/13용 신규 에러코드(담기/투표/마감/확정) 추가 예정.

## Data Models (FC-8~13 관련)
### place (V12)
- `place_id`(네이버), `name`, `branch`, `category`, `category_label`(한식/일식/.../기타)
- `address`, `latitude`, `longitude`, `location_point`(geography 4326)
- `has_room`, `has_group_seat`, `has_parking`, `reservable`, `max_group_size`
- `vibe TEXT[]`, `occasion TEXT[]`, `size_fit`, `summary`, `naver_url`, `rating`, `review_count`

### subway_edge (V17)
- `from_station_id`, `to_station_id` (→ subway_station)
- `weight_sec`(초), `edge_type`(RIDE=역간 / TRANSFER=환승)
- UNIQUE(from, to, edge_type), 인접리스트 로딩 가정

### meeting_participant (V11)
- `meeting_id`, `member_id`, `latitude`, `longitude`(nullable, V15), `attendance_status`
