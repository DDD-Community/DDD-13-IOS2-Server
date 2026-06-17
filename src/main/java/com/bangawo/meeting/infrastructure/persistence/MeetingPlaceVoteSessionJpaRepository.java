package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingPlaceVoteSessionJpaRepository
        extends JpaRepository<MeetingPlaceVoteSessionJpaEntity, Long> {

    Optional<MeetingPlaceVoteSessionJpaEntity> findByMeetingId(Long meetingId);

    @Query("SELECT s FROM MeetingPlaceVoteSessionJpaEntity s WHERE s.status = 'IN_PROGRESS' AND s.deadline < :now")
    List<MeetingPlaceVoteSessionJpaEntity> findInProgressWithExpiredDeadline(LocalDateTime now);
}
