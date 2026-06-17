# U2 추천(FC-8) — Domain Entities

## 신규 컨텍스트 `com.bangawo.place`

### Place (domain, 읽기 전용 — 쓰기 경로 없음, 데이터는 외부 파이프라인 적재)
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long | 내부 PK |
| placeId | Long | 네이버 place_id |
| name | String | 상호명 |
| categoryLabel | String | 한식/중식/.../뷔페/기타 |
| address | String | 도로명주소 |
| latitude/longitude | double | Coordinate 재사용(global.common) |
| vibe | List\<String\> | 분위기 태그 |
| occasion | List\<String\> | **기존 컬럼 그대로 사용**(V12). AI가 붙인 자유 태그(예: 회식/가족모임/스터디/친구모임...). 신규 컬럼 없음 |
| reservable | Boolean | NULL=정보없음 |
| hasParking | Boolean | NULL=정보없음 |
| rating | Double | NULL 가능 |

도메인 메서드:
- `matchesOccasion(themeTagDisplayName)`: occasion.contains(themeTagDisplayName) — **themeTagCode가 아니라 theme_tag.display_name과 비교**(occasion 데이터가 "회식"/"스터디" 같은 한글 표시명과 일치)
- `matchesCategory(categories)`: categories가 null/empty면 false, 아니면 categories.contains(categoryLabel)
- `vibeOverlap(vibes)`: vibes가 null/empty면 0.0, 아니면 |vibe ∩ vibes| / |vibes|

> **데이터 확인 결과**(`/Users/ym/dev/DDD/Data/pipeline/output/place_export.csv`, 2299건 전수): occasion 값 빈도 상위 — 친구모임(1943)/회식(802)/데이트(793)/단체모임(780)/카페타임(523)/혼밥(407)/점심식사(331)/가족모임(300)/특별한날(271)/혼술(266)/스터디(198). 기존 theme_tag 8종 중 **회식·가족모임·스터디**는 occasion 값과 정확히 일치. 나머지(친구모임/데이트/단체모임 등)는 매칭 안 됨 — theme_tag 확장 여부는 User 결정 사항(범위 외, 별도 논의)

### RecommendationCandidate (domain VO)
- `place: Place`, `nearestStationId: Long` — PlaceRepository.findCandidates 결과 1건(거리는 nearestStationId 결정에만 사용, 별도 보관 안 함)

### ScoredCandidate (domain VO)
- `candidate: RecommendationCandidate`, `score: double`

### PlaceScorer (domain service, 순수함수)
- `score(candidates, themeTagDisplayName, categories, vibes) -> List<ScoredCandidate>` (PlaceSelectionService가 `meeting.themeTagCode`를 `ThemeTagRepository`로 displayName 변환 후 전달)
- `score = 0.5*occasion + 0.25*category + 0.15*vibe + 0.1*rating`
- rating 정규화: 후보집합 내 non-null rating의 min-max. null → 0.5. min==max(전부 동일하거나 1건)인 경우 → 1.0

### PlaceOption (domain, 정적 헬퍼)
- `categories(): List<String>` → `CategoryLabel`(global.common) 값 그대로
- 호출부에서 `vibes()`는 `PlaceRepository.findDistinctVibes()` 직접 사용 (PlaceOption은 카테고리 상수 제공 역할만)

## `com.bangawo.meeting` 확장

### MeetingPlaceRecommendation (domain)
| 필드 | 비고 |
|---|---|
| id, meetingId, placeId | |
| rank | 1~15 |
| score | double |
| nearestStationId | 3개 후보역 중 최근접 |
| createdAt | |

### subway 확장 (기존 컴포넌트 내부 수정, 시그니처 불변)
- `StationCandidate` record에 `stationId: Long` 필드 추가 (그룹화된 station_name 내 대표 station_id, `MIN(station_id)`)
- `findCandidatesNearMeetingCenter(meetingId, limit)` 내부 SQL에 반경 사다리(2km→4km→6km) CTE 추가 — **메서드 시그니처 변경 없음** (컴포넌트 설계서 "기존" 명시와 일치)

## 테이블 (신규)
- V20: `meeting_place_recommendation`(erd.md 그대로)
- place 테이블 변경 없음 (기존 occasion 재사용)
