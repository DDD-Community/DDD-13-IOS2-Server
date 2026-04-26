package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findBySocialProviderAndSocialUserId(SocialProvider provider, String socialUserId);
}
