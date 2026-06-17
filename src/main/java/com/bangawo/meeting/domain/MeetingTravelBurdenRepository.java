package com.bangawo.meeting.domain;

import java.util.List;

public interface MeetingTravelBurdenRepository {
    void saveAll(List<MeetingTravelBurden> burdens);
    List<MeetingTravelBurden> findByMeetingId(Long meetingId);
    List<MeetingTravelBurden> findByMeetingIdAndPlaceId(Long meetingId, Long placeId);
}
