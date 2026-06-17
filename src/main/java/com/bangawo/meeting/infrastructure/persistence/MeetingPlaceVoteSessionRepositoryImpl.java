package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlaceVoteSession;
import com.bangawo.meeting.domain.MeetingPlaceVoteSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingPlaceVoteSessionRepositoryImpl implements MeetingPlaceVoteSessionRepository {

    private final MeetingPlaceVoteSessionJpaRepository jpaRepository;

    @Override
    public MeetingPlaceVoteSession save(MeetingPlaceVoteSession session) {
        return jpaRepository.save(MeetingPlaceVoteSessionJpaEntity.from(session)).toDomain();
    }

    @Override
    public Optional<MeetingPlaceVoteSession> findByMeetingId(Long meetingId) {
        return jpaRepository.findByMeetingId(meetingId)
                .map(MeetingPlaceVoteSessionJpaEntity::toDomain);
    }

    @Override
    public List<MeetingPlaceVoteSession> findInProgressWithExpiredDeadline() {
        return jpaRepository.findInProgressWithExpiredDeadline(LocalDateTime.now())
                .stream()
                .map(MeetingPlaceVoteSessionJpaEntity::toDomain)
                .toList();
    }
}
