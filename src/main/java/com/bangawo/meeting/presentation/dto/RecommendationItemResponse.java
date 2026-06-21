package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceSummary;

public record RecommendationItemResponse(
        int rank,
        PlaceSummary place,
        double score,
        Long nearestStationId
) {}
