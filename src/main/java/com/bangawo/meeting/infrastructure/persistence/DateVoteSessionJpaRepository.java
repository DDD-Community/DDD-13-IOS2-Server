package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DateVoteSessionJpaRepository extends JpaRepository<DateVoteSessionJpaEntity, Long> {
    Optional<DateVoteSessionJpaEntity> findByMeetingId(Long meetingId);
    List<DateVoteSessionJpaEntity> findByStatusAndDeadlineBefore(SessionStatus status, LocalDate date);
}
