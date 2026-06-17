package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingPlacePickJpaRepository extends JpaRepository<MeetingPlacePickJpaEntity, Long> {
    boolean existsByMeetingIdAndMemberIdAndPlaceId(Long meetingId, Long memberId, Long placeId);
    void deleteByMeetingIdAndMemberIdAndPlaceId(Long meetingId, Long memberId, Long placeId);
    List<MeetingPlacePickJpaEntity> findByMeetingId(Long meetingId);
    boolean existsByMeetingId(Long meetingId);
    int countByMeetingIdAndMemberId(Long meetingId, Long memberId);
}
