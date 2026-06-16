package com.bangawo.place.domain;

import java.util.List;

public interface PlaceRepository {
    List<RecommendationCandidate> findCandidates(List<Long> midpointStationIds, double radiusMeters,
                                                  Boolean reservable, Boolean parking);

    List<String> findDistinctVibes();

    List<Place> findByIds(List<Long> ids);
}
