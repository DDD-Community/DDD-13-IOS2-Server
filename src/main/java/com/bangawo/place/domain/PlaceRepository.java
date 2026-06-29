package com.bangawo.place.domain;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {
    List<RecommendationCandidate> findCandidates(List<Long> midpointStationIds, double radiusMeters,
                                                  Boolean reservable, Boolean parking);

    List<String> findDistinctVibes();

    List<Place> findByIds(List<Long> ids);

    Optional<Place> findById(Long id);

    List<PlaceWithDistance> findNearby(double latitude, double longitude,
                                       double radiusMeters, String categoryLabel, int limit);
}
