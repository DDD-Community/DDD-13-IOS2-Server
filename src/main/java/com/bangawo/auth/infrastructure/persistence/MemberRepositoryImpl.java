package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.auth.domain.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** MemberRepository 도메인 인터페이스의 JPA 구현 */
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository jpaRepository;
    private final MemberMapper mapper;

    @Override
    public Optional<Member> findByProviderAndSocialUserId(SocialProvider provider, String socialUserId) {
        return jpaRepository.findBySocialProviderAndSocialUserId(provider, socialUserId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = mapper.toEntity(member);
        MemberJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
