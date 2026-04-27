package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.auth.domain.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    @Override
    public Optional<Member> findByProviderAndSocialUserId(SocialProvider provider, String socialUserId) {
        return jpaRepository.findBySocialProviderAndSocialUserId(provider, socialUserId)
                .map(MemberJpaEntity::toDomain);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id)
                .map(MemberJpaEntity::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity saved = jpaRepository.save(MemberJpaEntity.from(member));
        return saved.toDomain();
    }
}
