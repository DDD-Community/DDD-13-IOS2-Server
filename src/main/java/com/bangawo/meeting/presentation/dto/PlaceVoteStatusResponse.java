package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceSummary;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceVoteStatusResponse(
        LocalDateTime deadline,
        String sessionStatus,
        int totalParticipants,
        int votedCount,
        List<MemberVoteStatus> memberStatuses,
        List<CandidateVoteInfo> candidates
) {
    public record MemberVoteStatus(
            Long memberId,
            String name,
            boolean completed
    ) {}

    public record CandidateVoteInfo(
            PlaceSummary place,
            int voteCount,
            boolean isMyVote
    ) {}
}
