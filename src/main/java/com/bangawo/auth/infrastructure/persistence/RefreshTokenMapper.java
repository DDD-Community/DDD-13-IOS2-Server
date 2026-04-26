package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.RefreshToken;
import org.springframework.stereotype.Component;

/** RefreshToken 도메인 엔티티 ↔ JPA 엔티티 변환 */
@Component
public class RefreshTokenMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .tokenHash(entity.getTokenHash())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        return RefreshTokenJpaEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .tokenHash(domain.getTokenHash())
                .expiresAt(domain.getExpiresAt())
                .revokedAt(domain.getRevokedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
