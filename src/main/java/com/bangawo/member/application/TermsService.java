package com.bangawo.member.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.member.domain.terms.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final TermsAgreementRepository agreementRepository;

    /** 현재 유효 약관 전체 조회 */
    public List<Terms> getCurrentTerms() {
        return termsRepository.findAllCurrent();
    }

    /** 약관 동의 처리 (이미 동의한 건 스킵) */
    @Transactional
    public void agreeTerms(Long memberId, List<Long> termsIds) {
        List<Terms> termsList = termsRepository.findAllById(termsIds);
        for (Terms terms : termsList) {
            if (!agreementRepository.existsByMemberIdAndTermsId(memberId, terms.getId())) {
                agreementRepository.save(TermsAgreement.builder()
                        .memberId(memberId)
                        .termsId(terms.getId())
                        .agreedAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    /** 필수 약관 전체 동의 여부 확인 */
    public boolean hasAgreedAllRequired(Long memberId) {
        List<Terms> required = termsRepository.findAllCurrent().stream()
                .filter(Terms::isRequired).toList();
        for (Terms terms : required) {
            if (!agreementRepository.existsByMemberIdAndTermsId(memberId, terms.getId()))
                return false;
        }
        return true;
    }

    /** 필수 약관 미동의 시 예외 발생 */
    public void validateRequiredTermsAgreed(Long memberId) {
        if (!hasAgreedAllRequired(memberId))
            throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
    }
}
