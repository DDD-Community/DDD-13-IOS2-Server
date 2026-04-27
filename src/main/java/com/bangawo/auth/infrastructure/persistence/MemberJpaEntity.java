package com.bangawo.auth.infrastructure.persistence;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberStatus;
import com.bangawo.auth.domain.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 회원 JPA 엔티티. DB member 테이블 매핑. */
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

    @Column(name = "is_registered", nullable = false)
    private boolean isRegistered;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** 도메인 → JPA 엔티티 */
    public static MemberJpaEntity from(Member domain) {
        MemberJpaEntity entity = new MemberJpaEntity();
        entity.id = domain.getId();
        entity.socialProvider = domain.getSocialProvider();
        entity.socialUserId = domain.getSocialUserId();
        entity.email = domain.getEmail();
        entity.nickname = domain.getNickname();
        entity.profileImageUrl = domain.getProfileImageUrl();
        entity.status = domain.getStatus();
        entity.isRegistered = domain.isRegistered();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    /** JPA 엔티티 → 도메인 */
    public Member toDomain() {
        return Member.builder()
                .id(id)
                .socialProvider(socialProvider)
                .socialUserId(socialUserId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .status(status)
                .isRegistered(isRegistered)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
