package com.bangawo.place.presentation.dto;

import com.bangawo.place.domain.PlaceWithDistance;

import java.util.List;

public record PlaceNearbyResponse(
        Long placeId,
        String name,
        String categoryLabel,
        String address,
        Double latitude,
        Double longitude,
        List<String> vibe,
        List<String> occasion,
        Boolean reservable,
        Boolean hasParking,
        Double rating,
        Double distanceMeters
) {
    public static PlaceNearbyResponse from(PlaceWithDistance pwd) {
        var p = pwd.place();
        return new PlaceNearbyResponse(
                p.getId(),
                p.getName(),
                p.getCategoryLabel(),
                p.getAddress(),
                p.getLatitude(),
                p.getLongitude(),
                p.getVibe(),
                p.getOccasion(),
                p.getReservable(),
                p.getHasParking(),
                p.getRating(),
                pwd.distanceMeters()
        );
    }
}
