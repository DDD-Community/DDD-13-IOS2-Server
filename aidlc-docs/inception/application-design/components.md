# Components — FC-8~13

## place 컨텍스트 (신규 `com.bangawo.place`)
| Component | Layer | 책임 |
|---|---|---|
| `Place` | domain | 장소 도메인 모델(place_id, name, category_label, address, 좌표, vibe[], theme_codes[], reservable/parking, rating). 태그 매칭 헬퍼 |
| `PlaceRepository` | domain (port) | 추천 후보 PostGIS 조회(반경+하드필터, 최근접역·거리 포함). vibe distinct 조회 |
| `RecommendationCandidate` | domain | 후보 1건 VO(place + 직선거리 + 최근접 역) |
| `PlaceScorer` | domain service | 순수 스코어링 함수(occasion/category/vibe/rating 가중합, min-max 정규화) |
| `PlaceOption` | domain | 선택지 상수(고정 11 카테고리) + vibe 목록 |
| `PlaceRepositoryImpl`, `PlaceJpaEntity`, `PlaceJpaRepository` | infrastructure | PostGIS 네이티브 쿼리 구현 |

## subway 컨텍스트 (확장)
| Component | Layer | 책임 |
|---|---|---|
| `SubwayGraph` | domain | 인접리스트(역→엣지). 부팅 시 subway_edge 로드 |
| `ShortestPathService` | domain service | 단일출발 다익스트라 → 모든 역까지 소요초·환승수 |
| `PathResult` | domain | (목적지역 → 소요초, 환승수) 맵 VO |
| `SubwayEdgeRepository` | domain (port) | subway_edge 전체 로드 |
| `SubwayGraphLoader` | infrastructure | ApplicationRunner — 부팅 시 그래프 적재 |
| `SubwayStationRepository`(기존) | domain | 최근접역/중간역 후보(PostGIS) |

## meeting 컨텍스트 (확장)
| Component | Layer | 책임 |
|---|---|---|
| `LocationStatus`(교체) | domain | BEFORE/RECOMMENDED/VOTING/CONFIRMED |
| `Meeting`(확장) | domain | 상태 전이 + `dateVoteStatus==COMPLETED` 가드, categories/vibes 보유 |
| `MeetingPlaceRecommendation` | domain | 추천 15 스냅샷(rank, score, 귀속역, placeId) |
| `MeetingPlacePick` | domain | 담기(모임원×place, 담은시각) |
| `MeetingPlaceVoteSession` | domain | 투표세션(시작일, 마감일, 상태) |
| `MeetingPlaceVote` | domain | 투표(모임원×place, 익명집계) |
| `MeetingTravelBurden` | domain | 이동부담 스냅샷(member×place: 소요초, 환승수) |
| `MeetingConfirmedPlace` | domain | 확정 장소 고정 저장 |
| 각 `*Repository`(port) + infra(`*JpaEntity/JpaRepository/RepositoryImpl`) | domain/infra | 영속성 |
| `PlaceSelectionScheduler`(확장) | infrastructure | 담기마감/투표마감 배치 |

## global (확장)
- `ErrorCode`(확장): FC-9/11/12/13 신규 에러코드

## 인터페이스 원칙
- Repository 인터페이스는 도메인 모델 반환(JpaEntity 금지)
- meeting → subway/place 는 **도메인 포트**를 통해 호출(직접 JPA 의존 금지)
- 스코어링·최단경로는 순수 도메인 서비스(테스트 용이, PBT 대상)
