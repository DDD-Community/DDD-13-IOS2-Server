package com.bangawo.member.presentation.dto;

import com.bangawo.auth.domain.Member;

/** 회원 프로필 응답 */
public record MemberResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        String socialProvider,
        boolean registrationCompleted
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getSocialProvider().name(),
                member.isRegistered()
        );
    }

    public static MemberResponse from(Member member, String resolvedImageUrl) {
        return new MemberResponse(
                member.getId(),
                member.getNickname(),
                resolvedImageUrl,
                member.getSocialProvider().name(),
                member.isRegistered()
        );
    }
}
