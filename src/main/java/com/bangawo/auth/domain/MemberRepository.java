package com.bangawo.auth.domain;

import java.util.Optional;

/** 회원 저장소 인터페이스. 구현은 infrastructure 레이어에서. */
public interface MemberRepository {
    Optional<Member> findByProviderAndSocialUserId(SocialProvider provider, String socialUserId);
    Optional<Member> findById(Long id);
    Member save(Member member);
}
