package com.bangawo.subway.infrastructure.persistence;

import com.bangawo.subway.domain.StationCandidate;
import com.bangawo.subway.domain.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubwayStationRepositoryImpl implements SubwayStationRepository {

    private final SubwayStationJpaRepository jpaRepository;

    @Override
    public List<StationCandidate> findCandidatesNearMeetingCenter(Long meetingId, int limit) {
        return jpaRepository.findRawCandidatesNearMeetingCenter(meetingId, limit)
                .stream()
                .map(row -> new StationCandidate(
                        ((Number) row[3]).longValue(),
                        (String) row[0],
                        (String) row[1],
                        ((BigDecimal) row[2]).doubleValue(),
                        ((Number) row[4]).doubleValue(),
                        ((Number) row[5]).doubleValue()
                ))
                .toList();
    }

    @Override
    public Optional<Long> findNearestStationId(double latitude, double longitude) {
        return jpaRepository.findNearestStationId(latitude, longitude);
    }
}
