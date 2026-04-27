package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.RefreshToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Refresh Token JPA 엔티티. 토큰 원문이 아닌 해시(token_hash)만 저장. */
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 도메인 → JPA 엔티티 */
    public static RefreshTokenJpaEntity from(RefreshToken domain) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.id = domain.getId();
        entity.memberId = domain.getMemberId();
        entity.tokenHash = domain.getTokenHash();
        entity.expiresAt = domain.getExpiresAt();
        entity.revokedAt = domain.getRevokedAt();
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }

    /** JPA 엔티티 → 도메인 */
    public RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(id)
                .memberId(memberId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revokedAt(revokedAt)
                .createdAt(createdAt)
                .build();
    }
}
