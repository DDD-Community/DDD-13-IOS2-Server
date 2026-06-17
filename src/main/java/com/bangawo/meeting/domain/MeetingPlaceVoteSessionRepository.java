package com.bangawo.meeting.domain;

import java.util.List;
import java.util.Optional;

public interface MeetingPlaceVoteSessionRepository {
    MeetingPlaceVoteSession save(MeetingPlaceVoteSession session);
    Optional<MeetingPlaceVoteSession> findByMeetingId(Long meetingId);
    List<MeetingPlaceVoteSession> findInProgressWithExpiredDeadline();
}
