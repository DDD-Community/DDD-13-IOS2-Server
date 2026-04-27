package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.RefreshToken;
import com.bangawo.auth.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity saved = jpaRepository.save(RefreshTokenJpaEntity.from(refreshToken));
        return saved.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public void revokeAllByMemberId(Long memberId) {
        jpaRepository.revokeAllByMemberId(memberId);
    }
}
