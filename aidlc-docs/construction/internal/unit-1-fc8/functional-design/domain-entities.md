# U1 — Domain Entities

## LocationStatus (enum 교체)
```java
public enum LocationStatus { BEFORE, RECOMMENDED, VOTING, CONFIRMED }
```

## Meeting (확장)
- 필드 추가: `categoryLabels: List<String>`, `vibes: List<String>`
- `create(groupId, name, themeTagCode, categoryLabels, vibes)` 정적 팩토리 확장
- 가드/전환 메서드: `assertCanStartLocationPhase()`, `completeRecommendation()`, `toVoting()`, `toConfirmed()`
- `computeListStatus(today)`: CONFIRMED 판정 조건을 `locationStatus == CONFIRMED`로 갱신

## meeting 테이블 (V18 컬럼 추가)
| 컬럼 | 타입 | 설명 |
|---|---|---|
| category_labels | TEXT[] | FC-8 추천용 음식 카테고리 선호 |
| vibes | TEXT[] | FC-8 추천용 분위기 태그 선호 |
