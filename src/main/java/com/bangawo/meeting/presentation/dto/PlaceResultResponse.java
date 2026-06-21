package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceSummary;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceResultResponse(
        PlaceSummary place,
        LocalDateTime confirmedAt,
        List<CandidateResult> candidates
) {
    public record CandidateResult(
            PlaceSummary place,
            int voteCount,
            long totalSeconds,
            long totalTransfers
    ) {}
}
