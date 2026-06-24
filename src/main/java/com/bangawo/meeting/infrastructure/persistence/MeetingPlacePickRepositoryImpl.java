package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlacePick;
import com.bangawo.meeting.domain.MeetingPlacePickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetingPlacePickRepositoryImpl implements MeetingPlacePickRepository {

    private final MeetingPlacePickJpaRepository jpaRepository;

    @Override
    public MeetingPlacePick save(MeetingPlacePick pick) {
        return jpaRepository.save(MeetingPlacePickJpaEntity.from(pick)).toDomain();
    }

    @Override
    public List<MeetingPlacePick> saveAll(List<MeetingPlacePick> picks) {
        return jpaRepository.saveAll(picks.stream().map(MeetingPlacePickJpaEntity::from).toList())
                .stream().map(MeetingPlacePickJpaEntity::toDomain).toList();
    }

    @Override
    public boolean existsByMeetingIdAndMemberIdAndPlaceId(Long meetingId, Long memberId, Long placeId) {
        return jpaRepository.existsByMeetingIdAndMemberIdAndPlaceId(meetingId, memberId, placeId);
    }

    @Override
    @Transactional
    public void deleteByMeetingIdAndMemberIdAndPlaceId(Long meetingId, Long memberId, Long placeId) {
        jpaRepository.deleteByMeetingIdAndMemberIdAndPlaceId(meetingId, memberId, placeId);
    }

    @Override
    public List<MeetingPlacePick> findByMeetingId(Long meetingId) {
        return jpaRepository.findByMeetingId(meetingId).stream()
                .map(MeetingPlacePickJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMeetingId(Long meetingId) {
        return jpaRepository.existsByMeetingId(meetingId);
    }

    @Override
    public int countByMeetingIdAndMemberId(Long meetingId, Long memberId) {
        return jpaRepository.countByMeetingIdAndMemberId(meetingId, memberId);
    }
}
