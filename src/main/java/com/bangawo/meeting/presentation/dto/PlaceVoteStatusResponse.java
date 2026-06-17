package com.bangawo.meeting.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceVoteStatusResponse(
        LocalDateTime deadline,
        String sessionStatus,
        int totalParticipants,
        int votedCount,
        List<CandidateVoteInfo> candidates
) {
    public record CandidateVoteInfo(
            Long placeId,
            int voteCount,
            boolean isMyVote,
            List<MemberBurdenInfo> travelBurdens
    ) {}

    public record MemberBurdenInfo(
            Long memberId,
            int seconds,
            int transfers,
            boolean isLongest
    ) {}
}
