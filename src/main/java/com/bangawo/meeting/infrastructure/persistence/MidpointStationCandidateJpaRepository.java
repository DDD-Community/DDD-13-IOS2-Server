package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MidpointStationCandidateJpaRepository extends JpaRepository<MidpointStationCandidateJpaEntity, Long> {
    List<MidpointStationCandidateJpaEntity> findByMeetingIdOrderByRank(Long meetingId);
}
