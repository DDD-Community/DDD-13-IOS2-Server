package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlaceVote;
import com.bangawo.meeting.domain.MeetingPlaceVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetingPlaceVoteRepositoryImpl implements MeetingPlaceVoteRepository {

    private final MeetingPlaceVoteJpaRepository jpaRepository;

    @Override
    public void saveAll(List<MeetingPlaceVote> votes) {
        jpaRepository.saveAll(votes.stream().map(MeetingPlaceVoteJpaEntity::from).toList());
    }

    @Override
    public List<MeetingPlaceVote> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId).stream()
                .map(MeetingPlaceVoteJpaEntity::toDomain)
                .toList();
    }

    @Override
    public int countByMemberIdAndSessionId(Long memberId, Long sessionId) {
        return jpaRepository.countByMemberIdAndSessionId(memberId, sessionId);
    }

    @Override
    @Transactional
    public void deleteBySessionIdAndMemberId(Long sessionId, Long memberId) {
        jpaRepository.deleteBySessionIdAndMemberId(sessionId, memberId);
    }

    @Override
    public long countDistinctVotersBySessionId(Long sessionId) {
        return jpaRepository.countDistinctVotersBySessionId(sessionId);
    }
}
