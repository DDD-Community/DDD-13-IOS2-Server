package com.bangawo.member.infrastructure.persistence.terms;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TermsAgreementJpaRepository extends JpaRepository<TermsAgreementJpaEntity, Long> {
    List<TermsAgreementJpaEntity> findAllByMemberId(Long memberId);
    boolean existsByMemberIdAndTermsId(Long memberId, Long termsId);
}
