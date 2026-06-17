package com.bangawo.meeting.domain;

import java.util.Optional;

public interface MeetingConfirmedPlaceRepository {
    MeetingConfirmedPlace save(MeetingConfirmedPlace confirmedPlace);
    Optional<MeetingConfirmedPlace> findByMeetingId(Long meetingId);
}
