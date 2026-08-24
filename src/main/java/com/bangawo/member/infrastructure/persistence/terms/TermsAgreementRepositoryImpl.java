package com.bangawo.member.infrastructure.persistence.terms;

import com.bangawo.member.domain.terms.TermsAgreement;
import com.bangawo.member.domain.terms.TermsAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TermsAgreementRepositoryImpl implements TermsAgreementRepository {

    private final TermsAgreementJpaRepository jpaRepository;

    @Override
    public TermsAgreement save(TermsAgreement agreement) {
        return jpaRepository.save(TermsAgreementJpaEntity.from(agreement)).toDomain();
    }

    @Override
    public List<TermsAgreement> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberId(memberId).stream()
                .map(TermsAgreementJpaEntity::toDomain).toList();
    }

    @Override
    public boolean existsByMemberIdAndTermsId(Long memberId, Long termsId) {
        return jpaRepository.existsByMemberIdAndTermsId(memberId, termsId);
    }

    @Override
    public void deleteAllByMemberId(Long memberId) {
        jpaRepository.deleteAllByMemberId(memberId);
    }
}
