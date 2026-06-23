package com.bangawo.meeting.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PlaceRecommendRequest(
        @Schema(description = "검색 반경(km). 미입력 시 기본 2km, 최대 6km", example = "2.0", minimum = "0", maximum = "6")
        Double radiusKm
) {}
