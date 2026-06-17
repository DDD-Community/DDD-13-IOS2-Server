package com.bangawo.meeting.domain;

import java.util.List;

public interface MeetingPlaceVoteRepository {
    void saveAll(List<MeetingPlaceVote> votes);
    List<MeetingPlaceVote> findBySessionId(Long sessionId);
    int countByMemberIdAndSessionId(Long memberId, Long sessionId);
    void deleteBySessionIdAndMemberId(Long sessionId, Long memberId);
    long countDistinctVotersBySessionId(Long sessionId);
}
