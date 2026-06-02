package com.bangawo.auth.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 도메인 엔티티.
 * 소셜 로그인 정보 + 프로필을 통합한 핵심 엔티티.
 * JPA 엔티티(MemberJpaEntity)와 분리되어 비즈니스 로직만 담당.
 */
@Getter
public class Member {

    private Long id;
    private SocialProvider socialProvider;
    private String socialUserId;   // 공급자가 부여한 고유 ID
    private String email;          // nullable (애플은 비공개 가능)
    private String nickname;
    private String profileImageUrl;
    private MemberStatus status;
    private boolean isRegistered;  // 회원가입 완료 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Member(Long id, SocialProvider socialProvider, String socialUserId,
                  String email, String nickname, String profileImageUrl,
                  MemberStatus status, boolean isRegistered,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.socialProvider = socialProvider;
        this.socialUserId = socialUserId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.isRegistered = isRegistered;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 소셜 로그인으로 신규 회원 생성 (닉네임/프로필은 회원가입 단계에서 설정) */
    public static Member create(SocialProvider provider, String socialUserId, String email) {
        return Member.builder()
                .socialProvider(provider)
                .socialUserId(socialUserId)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .isRegistered(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateProfileImageUrl(String objectKey) {
        this.profileImageUrl = objectKey;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /** 회원가입 완료 처리 */
    public void completeRegistration() {
        this.isRegistered = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }
}
