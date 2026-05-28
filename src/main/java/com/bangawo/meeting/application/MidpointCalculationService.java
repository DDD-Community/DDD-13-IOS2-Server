package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.subway.domain.StationCandidate;
import com.bangawo.subway.domain.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MidpointCalculationService {

    private static final int CANDIDATE_LIMIT = 3;

    private final SubwayStationRepository subwayStationRepository;

    public List<StationCandidate> calculate(Long meetingId) {
        List<StationCandidate> candidates =
                subwayStationRepository.findCandidatesNearMeetingCenter(meetingId, CANDIDATE_LIMIT);
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.MIDPOINT_STATION_NOT_FOUND);
        }
        return candidates;
    }
}
