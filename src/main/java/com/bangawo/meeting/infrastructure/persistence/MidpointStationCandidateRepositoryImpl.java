package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MidpointStationCandidate;
import com.bangawo.meeting.domain.MidpointStationCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MidpointStationCandidateRepositoryImpl implements MidpointStationCandidateRepository {

    private final MidpointStationCandidateJpaRepository jpaRepository;

    @Override
    public void saveAll(List<MidpointStationCandidate> candidates) {
        jpaRepository.saveAll(candidates.stream().map(MidpointStationCandidateJpaEntity::from).toList());
    }

    @Override
    public List<MidpointStationCandidate> findByMeetingIdOrderByRank(Long meetingId) {
        return jpaRepository.findByMeetingIdOrderByRank(meetingId).stream()
                .map(MidpointStationCandidateJpaEntity::toDomain)
                .toList();
    }
}
