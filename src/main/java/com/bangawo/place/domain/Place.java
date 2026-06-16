package com.bangawo.place.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 장소 — 읽기 전용 도메인 모델. 데이터는 외부 파이프라인이 적재(이 애플리케이션은 쓰기 경로 없음).
 */
@Getter
public class Place {

    private Long id;
    private Long placeId;
    private String name;
    private String categoryLabel;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<String> vibe;
    private List<String> occasion;
    private Boolean reservable;
    private Boolean hasParking;
    private Double rating;

    @Builder
    public Place(Long id, Long placeId, String name, String categoryLabel, String address,
                 Double latitude, Double longitude, List<String> vibe, List<String> occasion,
                 Boolean reservable, Boolean hasParking, Double rating) {
        this.id = id;
        this.placeId = placeId;
        this.name = name;
        this.categoryLabel = categoryLabel;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vibe = vibe;
        this.occasion = occasion;
        this.reservable = reservable;
        this.hasParking = hasParking;
        this.rating = rating;
    }

    /**
     * @param themeTagDisplayName theme_tag.display_name (예: "회식", "스터디") — themeTagCode가 아님
     */
    public boolean matchesOccasion(String themeTagDisplayName) {
        return occasion != null && occasion.contains(themeTagDisplayName);
    }

    public boolean matchesCategory(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return false;
        }
        return categories.contains(categoryLabel);
    }

    public double vibeOverlap(List<String> vibes) {
        if (vibes == null || vibes.isEmpty()) {
            return 0.0;
        }
        if (vibe == null || vibe.isEmpty()) {
            return 0.0;
        }
        long overlap = vibe.stream().filter(vibes::contains).distinct().count();
        return (double) overlap / vibes.size();
    }
}
