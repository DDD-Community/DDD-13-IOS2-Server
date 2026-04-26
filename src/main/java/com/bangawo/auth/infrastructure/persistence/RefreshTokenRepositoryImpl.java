package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.RefreshToken;
import com.bangawo.auth.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** RefreshTokenRepository 도메인 인터페이스의 JPA 구현 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = mapper.toEntity(refreshToken);
        RefreshTokenJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    public void revokeAllByMemberId(Long memberId) {
        jpaRepository.revokeAllByMemberId(memberId);
    }
}
