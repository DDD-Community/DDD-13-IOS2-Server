package com.bangawo.auth.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** 회원 저장소 인터페이스. 구현은 infrastructure 레이어에서. */
public interface MemberRepository {
    Optional<Member> findByProviderAndSocialUserId(SocialProvider provider, String socialUserId);
    Optional<Member> findById(Long id);
    Member save(Member member);
    List<Member> findAllById(Set<Long> ids);
}
