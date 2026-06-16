package com.bangawo.place.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceTest {

    private Place placeWith(String categoryLabel, List<String> vibe, List<String> occasion) {
        return Place.builder()
                .id(1L).placeId(100L).name("테스트 식당")
                .categoryLabel(categoryLabel)
                .vibe(vibe)
                .occasion(occasion)
                .build();
    }

    @Test
    @DisplayName("occasion에 theme_tag displayName이 있으면 matchesOccasion true")
    void matchesOccasion_true() {
        Place place = placeWith("한식", null, List.of("회식", "친목"));
        assertThat(place.matchesOccasion("회식")).isTrue();
    }

    @Test
    @DisplayName("occasion이 null이면 matchesOccasion false")
    void matchesOccasion_nullOccasion() {
        Place place = placeWith("한식", null, null);
        assertThat(place.matchesOccasion("회식")).isFalse();
    }

    @Test
    @DisplayName("categories에 categoryLabel이 포함되면 matchesCategory true")
    void matchesCategory_true() {
        Place place = placeWith("한식", null, null);
        assertThat(place.matchesCategory(List.of("한식", "주점"))).isTrue();
    }

    @Test
    @DisplayName("categories가 비어있으면 matchesCategory false")
    void matchesCategory_emptyCategories() {
        Place place = placeWith("한식", null, null);
        assertThat(place.matchesCategory(List.of())).isFalse();
        assertThat(place.matchesCategory(null)).isFalse();
    }

    @Test
    @DisplayName("vibeOverlap = 교집합/모임vibes개수")
    void vibeOverlap_ratio() {
        Place place = placeWith("한식", List.of("왁자지껄", "넓은"), null);
        assertThat(place.vibeOverlap(List.of("왁자지껄", "조용한"))).isEqualTo(0.5);
    }

    @Test
    @DisplayName("vibes가 비어있으면 vibeOverlap 0")
    void vibeOverlap_emptyVibes() {
        Place place = placeWith("한식", List.of("왁자지껄"), null);
        assertThat(place.vibeOverlap(List.of())).isEqualTo(0.0);
        assertThat(place.vibeOverlap(null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("place.vibe가 null이면 vibeOverlap 0")
    void vibeOverlap_nullPlaceVibe() {
        Place place = placeWith("한식", null, null);
        assertThat(place.vibeOverlap(List.of("왁자지껄"))).isEqualTo(0.0);
    }
}
