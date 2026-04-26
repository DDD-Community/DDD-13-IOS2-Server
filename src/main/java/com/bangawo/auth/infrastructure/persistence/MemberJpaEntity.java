package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.MemberStatus;
import com.bangawo.auth.domain.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 JPA 엔티티.
 * DB의 member 테이블과 매핑. 도메인 로직은 Member 도메인 엔티티에서 처리.
 */
@Entity
@Table(name = "member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"social_provider", "social_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Column(name = "social_user_id", nullable = false)
    private String socialUserId;

    private String email;

    @Column(length = 20)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public MemberJpaEntity(Long id, SocialProvider socialProvider, String socialUserId,
                           String email, String nickname, String profileImageUrl,
                           MemberStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.socialProvider = socialProvider;
        this.socialUserId = socialUserId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
