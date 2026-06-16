package com.bangawo.place.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PlaceScorerTest {

    private final PlaceScorer scorer = new PlaceScorer();

    private Place place(Long id, String categoryLabel, List<String> vibe, List<String> occasion, Double rating) {
        return Place.builder()
                .id(id).placeId(id).name("place-" + id)
                .categoryLabel(categoryLabel).vibe(vibe).occasion(occasion).rating(rating)
                .build();
    }

    @Test
    @DisplayName("모든 항목 매칭 + rating 최고값이면 score=1.0에 근접")
    void score_fullMatch() {
        Place p = place(1L, "한식", List.of("왁자지껄"), List.of("회식"), 5.0);
        Place other = place(2L, "일식", List.of("조용한"), List.of("스터디"), 3.0);
        List<RecommendationCandidate> candidates = List.of(
                new RecommendationCandidate(p, 10L),
                new RecommendationCandidate(other, 11L)
        );

        List<ScoredCandidate> scored = scorer.score(candidates, "회식", List.of("한식"), List.of("왁자지껄"));

        ScoredCandidate first = scored.stream().filter(s -> s.candidate().place().getId().equals(1L)).findFirst().orElseThrow();
        assertThat(first.score()).isCloseTo(1.0, within(0.0001));
    }

    @Test
    @DisplayName("themeTag/categories/vibes 모두 불일치 + rating 최저값이면 score=0.0")
    void score_noMatch() {
        Place p = place(1L, "한식", List.of("왁자지껄"), List.of("회식"), 1.0);
        Place other = place(2L, "일식", List.of("조용한"), List.of("스터디"), 5.0);
        List<RecommendationCandidate> candidates = List.of(
                new RecommendationCandidate(p, 10L),
                new RecommendationCandidate(other, 11L)
        );

        List<ScoredCandidate> scored = scorer.score(candidates, "청첩장 모임", List.of("중식"), List.of("조용한"));

        ScoredCandidate first = scored.stream().filter(s -> s.candidate().place().getId().equals(1L)).findFirst().orElseThrow();
        assertThat(first.score()).isCloseTo(0.0, within(0.0001));
    }

    @Test
    @DisplayName("categories/vibes가 비어있으면 해당 항목 0점 처리")
    void score_emptyMeetingPreferences() {
        Place p = place(1L, "한식", List.of("왁자지껄"), List.of("회식"), null);
        List<RecommendationCandidate> candidates = List.of(new RecommendationCandidate(p, 10L));

        List<ScoredCandidate> scored = scorer.score(candidates, "회식", null, null);

        // occasion(0.5) + rating결측(0.1*0.5=0.05) = 0.55, category/vibe는 0
        assertThat(scored.get(0).score()).isCloseTo(0.55, within(0.0001));
    }

    @Test
    @DisplayName("rating이 전부 동일하거나 단일후보면 정규화 1.0")
    void score_ratingNoSpread() {
        Place p1 = place(1L, "한식", null, null, 4.0);
        Place p2 = place(2L, "일식", null, null, 4.0);
        List<RecommendationCandidate> candidates = List.of(
                new RecommendationCandidate(p1, 10L),
                new RecommendationCandidate(p2, 11L)
        );

        List<ScoredCandidate> scored = scorer.score(candidates, "해당없음", null, null);

        scored.forEach(s -> assertThat(s.score()).isCloseTo(0.1, within(0.0001)));
    }

    @Test
    @DisplayName("rating이 NULL이면 0.5로 처리")
    void score_missingRating() {
        Place withRating = place(1L, "한식", null, null, 5.0);
        Place withoutRating = place(2L, "일식", null, null, null);
        List<RecommendationCandidate> candidates = List.of(
                new RecommendationCandidate(withRating, 10L),
                new RecommendationCandidate(withoutRating, 11L)
        );

        List<ScoredCandidate> scored = scorer.score(candidates, "해당없음", null, null);

        ScoredCandidate missing = scored.stream()
                .filter(s -> s.candidate().place().getId().equals(2L)).findFirst().orElseThrow();
        assertThat(missing.score()).isCloseTo(0.1 * 0.5, within(0.0001));
    }
}
