package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteSession;
import com.bangawo.meeting.domain.DateVoteSessionRepository;
import com.bangawo.meeting.domain.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DateVoteSessionRepositoryImpl implements DateVoteSessionRepository {

    private final DateVoteSessionJpaRepository jpaRepository;

    @Override
    public DateVoteSession save(DateVoteSession session) {
        return jpaRepository.save(DateVoteSessionJpaEntity.from(session)).toDomain();
    }

    @Override
    public Optional<DateVoteSession> findByMeetingId(Long meetingId) {
        return jpaRepository.findByMeetingId(meetingId).map(DateVoteSessionJpaEntity::toDomain);
    }

    @Override
    public List<DateVoteSession> findActiveByDeadlineBefore(LocalDate date) {
        return jpaRepository.findByStatusAndDeadlineBefore(SessionStatus.ACTIVE, date)
                .stream()
                .map(DateVoteSessionJpaEntity::toDomain)
                .toList();
    }
}
