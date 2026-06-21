package com.bangawo.place.presentation.dto;

import com.bangawo.place.domain.Place;

/**
 * 장소 요약 — placeId만 내려주던 응답들에 임베드해 클라이언트 별도 조회를 없애기 위한 표시용 정보.
 */
public record PlaceSummary(
        Long placeId,
        String name,
        String categoryLabel,
        String address
) {
    public static PlaceSummary from(Place place) {
        if (place == null) {
            return null;
        }
        return new PlaceSummary(
                place.getId(),
                place.getName(),
                place.getCategoryLabel(),
                place.getAddress()
        );
    }
}
