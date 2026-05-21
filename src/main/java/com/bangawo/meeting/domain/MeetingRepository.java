package com.bangawo.meeting.domain;

import java.util.Optional;

public interface MeetingRepository {
    Meeting save(Meeting meeting);
    Optional<Meeting> findById(Long id);
}
