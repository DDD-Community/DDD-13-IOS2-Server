package com.bangawo.member.domain.terms;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/** 약관 동의 이력. DELETE 금지 (법적 증적). */
@Getter
@Builder
public class TermsAgreement {
    private Long id;
    private Long memberId;
    private Long termsId;
    private LocalDateTime agreedAt;
}
