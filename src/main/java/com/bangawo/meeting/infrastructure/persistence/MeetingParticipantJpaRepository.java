package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipantJpaEntity, Long> {
    Optional<MeetingParticipantJpaEntity> findByMeetingIdAndMemberId(Long meetingId, Long memberId);
    List<MeetingParticipantJpaEntity> findByMeetingId(Long meetingId);
    List<MeetingParticipantJpaEntity> findByMeetingIdIn(List<Long> meetingIds);
    boolean existsByMeetingId(Long meetingId);
    List<MeetingParticipantJpaEntity> findByMemberId(Long memberId);
}
