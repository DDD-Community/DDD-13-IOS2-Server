package com.bangawo.place.infrastructure.persistence;

import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.domain.RecommendationCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository jpaRepository;

    @Override
    public List<RecommendationCandidate> findCandidates(List<Long> midpointStationIds, double radiusMeters,
                                                          Boolean reservable, Boolean parking) {
        List<Object[]> rows = jpaRepository.findCandidateIdsNearStations(
                midpointStationIds, radiusMeters, reservable, parking);
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> nearestStationByPlaceId = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long placeId = ((Number) row[0]).longValue();
            Long stationId = ((Number) row[1]).longValue();
            nearestStationByPlaceId.put(placeId, stationId);
        }

        Map<Long, Place> placeById = jpaRepository.findAllById(nearestStationByPlaceId.keySet())
                .stream()
                .map(PlaceJpaEntity::toDomain)
                .collect(java.util.stream.Collectors.toMap(Place::getId, p -> p));

        return nearestStationByPlaceId.entrySet().stream()
                .filter(e -> placeById.containsKey(e.getKey()))
                .map(e -> new RecommendationCandidate(placeById.get(e.getKey()), e.getValue()))
                .toList();
    }

    @Override
    public List<String> findDistinctVibes() {
        return jpaRepository.findDistinctVibes();
    }

    @Override
    public List<Place> findByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(PlaceJpaEntity::toDomain)
                .toList();
    }
}
