package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingPlaceRecommendationJpaRepository
        extends JpaRepository<MeetingPlaceRecommendationJpaEntity, Long> {
    List<MeetingPlaceRecommendationJpaEntity> findByMeetingIdOrderByRank(Long meetingId);
}
