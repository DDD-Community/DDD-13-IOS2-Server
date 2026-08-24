package com.bangawo.auth.domain;

import java.util.Optional;

/** Refresh Token 저장소 인터페이스. */
public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void revokeAllByMemberId(Long memberId);
    /** 해당 회원의 토큰을 물리 삭제 (탈퇴 시 파기 전용) */
    void deleteAllByMemberId(Long memberId);
}
