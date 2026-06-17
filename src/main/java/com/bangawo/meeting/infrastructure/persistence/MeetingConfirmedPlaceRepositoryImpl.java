package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingConfirmedPlace;
import com.bangawo.meeting.domain.MeetingConfirmedPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingConfirmedPlaceRepositoryImpl implements MeetingConfirmedPlaceRepository {

    private final MeetingConfirmedPlaceJpaRepository jpaRepository;

    @Override
    public MeetingConfirmedPlace save(MeetingConfirmedPlace confirmedPlace) {
        return jpaRepository.save(MeetingConfirmedPlaceJpaEntity.from(confirmedPlace)).toDomain();
    }

    @Override
    public Optional<MeetingConfirmedPlace> findByMeetingId(Long meetingId) {
        return jpaRepository.findByMeetingId(meetingId)
                .map(MeetingConfirmedPlaceJpaEntity::toDomain);
    }
}
