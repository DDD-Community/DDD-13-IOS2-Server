package com.bangawo.meeting.presentation.dto;

public record RecommendationItemResponse(
        int rank,
        Long placeId,
        String name,
        String categoryLabel,
        double score,
        Long nearestStationId
) {}
