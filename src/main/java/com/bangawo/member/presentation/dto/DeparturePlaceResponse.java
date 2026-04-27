package com.bangawo.member.presentation.dto;

import com.bangawo.member.domain.departure.DeparturePlace;

/** 출발지 응답 */
public record DeparturePlaceResponse(
        Long id,
        String label,
        String address,
        double latitude,
        double longitude,
        boolean isDefault
) {
    public static DeparturePlaceResponse from(DeparturePlace place) {
        return new DeparturePlaceResponse(
                place.getId(), place.getLabel(), place.getAddress(),
                place.getCoordinate().getLatitude(), place.getCoordinate().getLongitude(),
                place.isDefault()
        );
    }
}
