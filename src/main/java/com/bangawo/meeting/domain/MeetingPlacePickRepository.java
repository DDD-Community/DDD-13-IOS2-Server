package com.bangawo.meeting.domain;

import java.util.List;

public interface MeetingPlacePickRepository {
    MeetingPlacePick save(MeetingPlacePick pick);
    boolean existsByMeetingIdAndMemberIdAndPlaceId(Long meetingId, Long memberId, Long placeId);
    void deleteByMeetingIdAndMemberIdAndPlaceId(Long meetingId, Long memberId, Long placeId);
    List<MeetingPlacePick> findByMeetingId(Long meetingId);
    boolean existsByMeetingId(Long meetingId);
    int countByMeetingIdAndMemberId(Long meetingId, Long memberId);
}
