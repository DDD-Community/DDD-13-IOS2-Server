package com.bangawo.meeting.presentation.dto;

import com.bangawo.meeting.domain.DateVoteStatus;
import com.bangawo.meeting.domain.LocationStatus;
import com.bangawo.meeting.domain.MeetingListStatus;

import java.util.List;

public record MeetingCardResponse(
        Long groupId,
        Long meetingId,
        String name,
        String themeTagCode,
        String themeTagDisplay,
        MeetingListStatus listStatus,
        LocationStatus locationStatus,
        DateVoteStatus dateVoteStatus,
        String locationAddress,
        int memberCount,
        List<MemberInfo> members
) {
    public record MemberInfo(
            Long memberId,
            String nickname,
            String profileImageUrl,
            String attendanceStatus
    ) {}
}
