package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingConfirmedPlaceJpaRepository
        extends JpaRepository<MeetingConfirmedPlaceJpaEntity, Long> {

    Optional<MeetingConfirmedPlaceJpaEntity> findByMeetingId(Long meetingId);
}
