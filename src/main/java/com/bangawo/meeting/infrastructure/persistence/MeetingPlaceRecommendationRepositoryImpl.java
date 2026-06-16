package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlaceRecommendation;
import com.bangawo.meeting.domain.MeetingPlaceRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetingPlaceRecommendationRepositoryImpl implements MeetingPlaceRecommendationRepository {

    private final MeetingPlaceRecommendationJpaRepository jpaRepository;

    @Override
    public void saveAll(List<MeetingPlaceRecommendation> recommendations) {
        jpaRepository.saveAll(recommendations.stream()
                .map(MeetingPlaceRecommendationJpaEntity::from)
                .toList());
    }

    @Override
    public List<MeetingPlaceRecommendation> findByMeetingIdOrderByRank(Long meetingId) {
        return jpaRepository.findByMeetingIdOrderByRank(meetingId).stream()
                .map(MeetingPlaceRecommendationJpaEntity::toDomain)
                .toList();
    }
}
