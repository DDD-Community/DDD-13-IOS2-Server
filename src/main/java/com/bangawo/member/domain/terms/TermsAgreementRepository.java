package com.bangawo.member.domain.terms;

import java.util.List;

/** 일반 DELETE 금지(INSERT only). 단, 회원 탈퇴 시 파기는 예외. */
public interface TermsAgreementRepository {
    TermsAgreement save(TermsAgreement agreement);
    List<TermsAgreement> findAllByMemberId(Long memberId);
    boolean existsByMemberIdAndTermsId(Long memberId, Long termsId);
    /** 해당 회원의 약관 동의 이력을 물리 삭제 (탈퇴 시 파기 전용) */
    void deleteAllByMemberId(Long memberId);
}
