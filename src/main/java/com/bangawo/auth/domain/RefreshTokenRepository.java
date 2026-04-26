package com.bangawo.auth.domain;

import java.util.Optional;

/** Refresh Token 저장소 인터페이스. */
public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void revokeAllByMemberId(Long memberId);
}
