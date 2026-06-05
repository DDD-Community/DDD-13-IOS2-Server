package com.bangawo.subway.infrastructure.persistence;

import com.bangawo.subway.domain.StationCandidate;
import com.bangawo.subway.domain.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SubwayStationRepositoryImpl implements SubwayStationRepository {

    private final SubwayStationJpaRepository jpaRepository;

    @Override
    public List<StationCandidate> findCandidatesNearMeetingCenter(Long meetingId, int limit) {
        return jpaRepository.findRawCandidatesNearMeetingCenter(meetingId, limit)
                .stream()
                .map(row -> new StationCandidate(
                        (String) row[0],
                        (String) row[1],
                        ((BigDecimal) row[2]).doubleValue()
                ))
                .toList();
    }
}
