package com.bangawo.meeting.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceResultResponse(
        Long placeId,
        String placeName,
        String address,
        LocalDateTime confirmedAt,
        List<CandidateResult> candidates
) {
    public record CandidateResult(
            Long placeId,
            int voteCount,
            long totalSeconds,
            long totalTransfers
    ) {}
}
