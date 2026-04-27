package com.bangawo.member.domain.terms;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/** 약관 도메인 엔티티 */
@Getter
@Builder
public class Terms {
    private Long id;
    private TermsType type;
    private String version;
    private String title;
    private String content;
    private boolean isRequired;
    private LocalDateTime effectiveFrom;
}
