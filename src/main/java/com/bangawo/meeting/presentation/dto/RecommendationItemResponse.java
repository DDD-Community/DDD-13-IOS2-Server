package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceDetailResponse;

public record RecommendationItemResponse(
        int rank,
        PlaceDetailResponse place,
        double score,
        Long nearestStationId
) {}
