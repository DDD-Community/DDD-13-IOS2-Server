package com.bangawo.subway.domain;

import java.util.List;
import java.util.Optional;

public interface SubwayStationRepository {
    List<StationCandidate> findCandidatesNearMeetingCenter(Long meetingId, int limit);
    Optional<Long> findNearestStationId(double latitude, double longitude);
    List<StationCoordinate> findCoordinatesByIds(List<Long> stationIds);
}
