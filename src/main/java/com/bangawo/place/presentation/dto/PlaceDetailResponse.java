package com.bangawo.place.presentation.dto;

import com.bangawo.place.domain.Place;

import java.util.List;

public record PlaceDetailResponse(
        Long placeId,
        String name,
        String categoryLabel,
        String address,
        String roadAddress,
        Double latitude,
        Double longitude,
        List<String> vibe,
        List<String> occasion,
        Boolean reservable,
        Boolean hasParking,
        Double rating,
        String businessHours,
        String holiday,
        String naverUrl
) {
    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getCategoryLabel(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getVibe(),
                place.getOccasion(),
                place.getReservable(),
                place.getHasParking(),
                place.getRating(),
                place.getBusinessHours(),
                place.getHoliday(),
                place.getNaverUrl()
        );
    }
}
