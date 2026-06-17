package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingPlaceVoteJpaRepository extends JpaRepository<MeetingPlaceVoteJpaEntity, Long> {

    List<MeetingPlaceVoteJpaEntity> findBySessionId(Long sessionId);

    int countByMemberIdAndSessionId(Long memberId, Long sessionId);

    @Modifying
    @Query("DELETE FROM MeetingPlaceVoteJpaEntity v WHERE v.sessionId = :sessionId AND v.memberId = :memberId")
    void deleteBySessionIdAndMemberId(@Param("sessionId") Long sessionId, @Param("memberId") Long memberId);

    @Query("SELECT COUNT(DISTINCT v.memberId) FROM MeetingPlaceVoteJpaEntity v WHERE v.sessionId = :sessionId")
    long countDistinctVotersBySessionId(@Param("sessionId") Long sessionId);
}
