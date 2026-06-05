package com.bangawo.meeting.domain;

import java.util.List;

import java.util.Optional;

public interface MeetingParticipantRepository {
    MeetingParticipant save(MeetingParticipant participant);
    void saveAll(List<MeetingParticipant> participants);
    Optional<MeetingParticipant> findByMeetingIdAndMemberId(Long meetingId, Long memberId);
    List<MeetingParticipant> findByMeetingId(Long meetingId);
    boolean existsByMeetingId(Long meetingId);
}
