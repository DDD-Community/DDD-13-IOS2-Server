package com.bangawo.meeting.domain;

import java.util.List;

public interface MeetingPlaceRecommendationRepository {
    void saveAll(List<MeetingPlaceRecommendation> recommendations);
    List<MeetingPlaceRecommendation> findByMeetingIdOrderByRank(Long meetingId);
}
