package com.bangawo.auth.application;

import com.bangawo.auth.domain.SocialProvider;

/** 소셜 공급자에서 가져온 사용자 정보 */
public record SocialUserInfo(
        SocialProvider provider,
        String socialUserId,  // 공급자가 부여한 고유 ID
        String email          // nullable
) {}
