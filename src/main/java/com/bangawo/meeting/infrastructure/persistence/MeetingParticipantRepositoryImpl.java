package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingParticipantRepositoryImpl implements MeetingParticipantRepository {

    private final MeetingParticipantJpaRepository jpaRepository;

    @Override
    public MeetingParticipant save(MeetingParticipant participant) {
        return jpaRepository.save(MeetingParticipantJpaEntity.from(participant)).toDomain();
    }

    @Override
    public void saveAll(List<MeetingParticipant> participants) {
        jpaRepository.saveAll(participants.stream().map(MeetingParticipantJpaEntity::from).toList());
    }

    @Override
    public Optional<MeetingParticipant> findByMeetingIdAndMemberId(Long meetingId, Long memberId) {
        return jpaRepository.findByMeetingIdAndMemberId(meetingId, memberId)
                .map(MeetingParticipantJpaEntity::toDomain);
    }

    @Override
    public List<MeetingParticipant> findByMeetingId(Long meetingId) {
        return jpaRepository.findByMeetingId(meetingId).stream()
                .map(MeetingParticipantJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<MeetingParticipant> findByMeetingIdIn(List<Long> meetingIds) {
        return jpaRepository.findByMeetingIdIn(meetingIds).stream()
                .map(MeetingParticipantJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMeetingId(Long meetingId) {
        return jpaRepository.existsByMeetingId(meetingId);
    }
}
