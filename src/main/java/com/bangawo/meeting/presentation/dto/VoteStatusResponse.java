package com.bangawo.meeting.presentation.dto;

import com.bangawo.meeting.domain.DateVoteStatus;
import com.bangawo.meeting.domain.SessionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record VoteStatusResponse(
        DateVoteStatus dateVoteStatus,
        SessionStatus sessionStatus,
        LocalDate deadline,
        List<VoteOptionInfo> options
) {
    public record VoteOptionInfo(
            Long optionId,
            LocalDateTime candidateDate,
            int voteCount,
            boolean isMyVote,
            List<VoterInfo> voters
    ) {}

    public record VoterInfo(
            Long memberId,
            String nickname,
            String profileImageUrl
    ) {}
}
