package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingTravelBurdenJpaRepository extends JpaRepository<MeetingTravelBurdenJpaEntity, Long> {

    List<MeetingTravelBurdenJpaEntity> findByMeetingId(Long meetingId);

    List<MeetingTravelBurdenJpaEntity> findByMeetingIdAndPlaceId(Long meetingId, Long placeId);
}
