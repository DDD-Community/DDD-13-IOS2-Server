package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DateVoteRecordJpaRepository extends JpaRepository<DateVoteRecordJpaEntity, Long> {
    List<DateVoteRecordJpaEntity> findByOptionIdIn(List<Long> optionIds);

    @Modifying
    @Query("DELETE FROM DateVoteRecordJpaEntity r WHERE r.optionId IN :optionIds AND r.memberId = :memberId")
    void deleteByOptionIdInAndMemberId(@Param("optionIds") List<Long> optionIds, @Param("memberId") Long memberId);

    @Query("SELECT COUNT(DISTINCT r.memberId) FROM DateVoteRecordJpaEntity r WHERE r.optionId IN :optionIds")
    long countDistinctMemberIdByOptionIdIn(@Param("optionIds") List<Long> optionIds);
}
