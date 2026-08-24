package com.bangawo.member.infrastructure.persistence.terms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TermsAgreementJpaRepository extends JpaRepository<TermsAgreementJpaEntity, Long> {
    List<TermsAgreementJpaEntity> findAllByMemberId(Long memberId);
    boolean existsByMemberIdAndTermsId(Long memberId, Long termsId);

    /** 해당 회원의 약관 동의 이력을 물리 삭제 (탈퇴 시 파기 전용) */
    @Modifying
    @Query("DELETE FROM TermsAgreementJpaEntity t WHERE t.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
