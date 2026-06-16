package com.bangawo.place.domain;

import java.util.List;
import java.util.Objects;

/**
 * FC-8 SOFT 스코어링 — 순수 함수. score = 0.5*occasion + 0.25*category + 0.15*vibe + 0.1*rating
 * rating은 후보집합 내 min-max 정규화. 결측(NULL)은 0.5, 동일/단일값(정규화 불가)은 1.0.
 */
public class PlaceScorer {

    private static final double WEIGHT_OCCASION = 0.5;
    private static final double WEIGHT_CATEGORY = 0.25;
    private static final double WEIGHT_VIBE = 0.15;
    private static final double WEIGHT_RATING = 0.1;
    private static final double MISSING_RATING_SCORE = 0.5;
    private static final double NO_SPREAD_RATING_SCORE = 1.0;

    /**
     * @param themeTagDisplayName theme_tag.display_name (예: "회식") — place.occasion과 직접 비교
     */
    public List<ScoredCandidate> score(List<RecommendationCandidate> candidates, String themeTagDisplayName,
                                        List<String> categories, List<String> vibes) {
        List<Double> ratings = candidates.stream()
                .map(c -> c.place().getRating())
                .filter(Objects::nonNull)
                .toList();
        double min = ratings.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = ratings.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        return candidates.stream()
                .map(c -> {
                    Place place = c.place();
                    double occasion = place.matchesOccasion(themeTagDisplayName) ? 1.0 : 0.0;
                    double category = place.matchesCategory(categories) ? 1.0 : 0.0;
                    double vibe = place.vibeOverlap(vibes);
                    double rating = normalizeRating(place.getRating(), min, max);
                    double score = WEIGHT_OCCASION * occasion + WEIGHT_CATEGORY * category
                            + WEIGHT_VIBE * vibe + WEIGHT_RATING * rating;
                    return new ScoredCandidate(c, score);
                })
                .toList();
    }

    private double normalizeRating(Double rating, double min, double max) {
        if (rating == null) {
            return MISSING_RATING_SCORE;
        }
        if (max == min) {
            return NO_SPREAD_RATING_SCORE;
        }
        return (rating - min) / (max - min);
    }
}
