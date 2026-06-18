package com.bangawo.meeting.presentation.dto;

import com.bangawo.meeting.domain.DateVoteStatus;
import com.bangawo.meeting.domain.LocationStatus;

import java.time.LocalDate;
import java.util.List;

public record MeetingDetailResponse(
        Long meetingId,
        String name,
        String themeTagCode,
        String themeTagDisplay,
        LocationStatus locationStatus,
        DateVoteStatus dateVoteStatus,
        LocalDate confirmedDate,
        List<MemberDetailInfo> members
) {
    public record MemberDetailInfo(
            Long memberId,
            String nickname,
            String profileImageUrl,
            boolean isHost,
            boolean isMe,
            String attendanceStatus,
            List<DeparturePlaceInfo> departurePlaces
    ) {}

    public record DeparturePlaceInfo(
            Long id,
            String label,
            String address,
            String roadAddress,
            String placeName,
            double latitude,
            double longitude,
            boolean isDefault
    ) {}
}
