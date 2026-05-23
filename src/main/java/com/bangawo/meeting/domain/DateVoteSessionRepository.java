package com.bangawo.meeting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DateVoteSessionRepository {
    DateVoteSession save(DateVoteSession session);
    Optional<DateVoteSession> findByMeetingId(Long meetingId);
    List<DateVoteSession> findActiveByDeadlineBefore(LocalDate date);
}
