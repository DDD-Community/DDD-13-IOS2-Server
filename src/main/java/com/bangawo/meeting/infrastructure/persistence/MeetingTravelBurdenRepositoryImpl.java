package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingTravelBurden;
import com.bangawo.meeting.domain.MeetingTravelBurdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetingTravelBurdenRepositoryImpl implements MeetingTravelBurdenRepository {

    private final MeetingTravelBurdenJpaRepository jpaRepository;

    @Override
    public void saveAll(List<MeetingTravelBurden> burdens) {
        jpaRepository.saveAll(burdens.stream().map(MeetingTravelBurdenJpaEntity::from).toList());
    }

    @Override
    public List<MeetingTravelBurden> findByMeetingId(Long meetingId) {
        return jpaRepository.findByMeetingId(meetingId).stream()
                .map(MeetingTravelBurdenJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<MeetingTravelBurden> findByMeetingIdAndPlaceId(Long meetingId, Long placeId) {
        return jpaRepository.findByMeetingIdAndPlaceId(meetingId, placeId).stream()
                .map(MeetingTravelBurdenJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByMemberId(Long memberId) {
        jpaRepository.deleteAllByMemberId(memberId);
    }
}
