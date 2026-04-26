package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.Member;
import org.springframework.stereotype.Component;

/** Member 도메인 엔티티 ↔ JPA 엔티티 변환 */
@Component
public class MemberMapper {

    public Member toDomain(MemberJpaEntity entity) {
        return Member.builder()
                .id(entity.getId())
                .socialProvider(entity.getSocialProvider())
                .socialUserId(entity.getSocialUserId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .profileImageUrl(entity.getProfileImageUrl())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MemberJpaEntity toEntity(Member domain) {
        return MemberJpaEntity.builder()
                .id(domain.getId())
                .socialProvider(domain.getSocialProvider())
                .socialUserId(domain.getSocialUserId())
                .email(domain.getEmail())
                .nickname(domain.getNickname())
                .profileImageUrl(domain.getProfileImageUrl())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
