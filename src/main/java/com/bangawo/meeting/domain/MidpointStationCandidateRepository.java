package com.bangawo.meeting.domain;

import java.util.List;

public interface MidpointStationCandidateRepository {
    void saveAll(List<MidpointStationCandidate> candidates);
    List<MidpointStationCandidate> findByMeetingIdOrderByRank(Long meetingId);
}
