package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

    private final MeetingJpaRepository jpaRepository;

    @Override
    public Meeting save(Meeting meeting) {
        return jpaRepository.save(MeetingJpaEntity.from(meeting)).toDomain();
    }

    @Override
    public Optional<Meeting> findById(Long id) {
        return jpaRepository.findById(id).map(MeetingJpaEntity::toDomain);
    }
}
