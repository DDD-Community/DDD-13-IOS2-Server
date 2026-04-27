package com.bangawo.member.domain.terms;

import java.util.List;

/** DELETE 금지. INSERT만 허용. */
public interface TermsAgreementRepository {
    TermsAgreement save(TermsAgreement agreement);
    List<TermsAgreement> findAllByMemberId(Long memberId);
    boolean existsByMemberIdAndTermsId(Long memberId, Long termsId);
}
