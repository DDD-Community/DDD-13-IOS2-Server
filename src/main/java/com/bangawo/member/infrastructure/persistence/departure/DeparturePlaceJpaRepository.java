package com.bangawo.member.infrastructure.persistence.departure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DeparturePlaceJpaRepository extends JpaRepository<DeparturePlaceJpaEntity, Long> {
    List<DeparturePlaceJpaEntity> findAllByMemberId(Long memberId);
    List<DeparturePlaceJpaEntity> findAllByMemberIdIn(List<Long> memberIds);
    Optional<DeparturePlaceJpaEntity> findByIdAndMemberId(Long id, Long memberId);
    int countByMemberId(Long memberId);

    @Modifying
    @Query("UPDATE DeparturePlaceJpaEntity d SET d.isDefault = false WHERE d.memberId = :memberId AND d.isDefault = true")
    void clearDefaultByMemberId(Long memberId);

    Optional<DeparturePlaceJpaEntity> findByMemberIdAndIsDefaultTrue(Long memberId);

    /** 해당 회원의 출발지를 물리 삭제 (탈퇴 시 파기 전용) */
    @Modifying
    @Query("DELETE FROM DeparturePlaceJpaEntity d WHERE d.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);
}
