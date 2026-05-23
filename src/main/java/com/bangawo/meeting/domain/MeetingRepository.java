package com.bangawo.meeting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository {
    Meeting save(Meeting meeting);
    Optional<Meeting> findById(Long id);
    List<Meeting> findLatestByGroupIdIn(List<Long> groupIds);
    List<Meeting> findExpiredMeetings(LocalDate today);
}
