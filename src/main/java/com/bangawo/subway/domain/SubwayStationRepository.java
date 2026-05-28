package com.bangawo.subway.domain;

import java.util.List;

public interface SubwayStationRepository {
    List<StationCandidate> findCandidatesNearMeetingCenter(Long meetingId, int limit);
}
