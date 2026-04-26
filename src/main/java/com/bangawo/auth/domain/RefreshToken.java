package com.bangawo.auth.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Refresh Token 도메인 엔티티.
 * 토큰 원문은 저장하지 않고 해시만 저장 (보안).
 * 만료/폐기 여부로 유효성 판단.
 */
@Getter
public class RefreshToken {

    private Long id;
    private Long memberId;
    private String tokenHash;      // SHA-256 해시
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt; // null이면 유효, 값이 있으면 폐기됨
    private LocalDateTime createdAt;

    @Builder
    public RefreshToken(Long id, Long memberId, String tokenHash,
                        LocalDateTime expiresAt, LocalDateTime revokedAt, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }
}
