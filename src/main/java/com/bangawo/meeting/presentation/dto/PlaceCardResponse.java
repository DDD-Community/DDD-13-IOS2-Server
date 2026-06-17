package com.bangawo.meeting.presentation.dto;

import java.util.List;

public record PlaceCardResponse(
        Long placeId,
        String name,
        String categoryLabel,
        String address,
        List<String> vibes,
        Integer cardDistance,
        int pickCount,
        boolean pickedByMe
) {}
