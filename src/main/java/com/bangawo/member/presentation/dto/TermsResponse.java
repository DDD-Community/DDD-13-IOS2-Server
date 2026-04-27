package com.bangawo.member.presentation.dto;

import com.bangawo.member.domain.terms.Terms;

/** 약관 응답 */
public record TermsResponse(
        Long id,
        String type,
        String title,
        String content,
        boolean required
) {
    public static TermsResponse from(Terms terms) {
        return new TermsResponse(
                terms.getId(), terms.getType().name(),
                terms.getTitle(), terms.getContent(), terms.isRequired()
        );
    }
}
