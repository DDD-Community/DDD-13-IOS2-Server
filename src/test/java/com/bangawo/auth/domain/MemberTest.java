package com.bangawo.auth.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    private Member activeMember() {
        return Member.builder()
                .id(1L)
                .socialProvider(SocialProvider.KAKAO)
                .socialUserId("kakao-1")
                .email("user@example.com")
                .nickname("닉네임")
                .profileImageUrl("profiles/1/a.png")
                .status(MemberStatus.ACTIVE)
                .isRegistered(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void withdraw_상태를_WITHDRAWN으로_전이하고_개인정보를_익명화한다() {
        Member member = activeMember();

        member.withdraw("withdrawn_uuid-1234");

        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(member.getDeletedAt()).isNotNull();
        assertThat(member.getNickname()).isNull();
        assertThat(member.getEmail()).isNull();
        assertThat(member.getProfileImageUrl()).isNull();
        assertThat(member.getSocialUserId()).isEqualTo("withdrawn_uuid-1234");
    }

    @Test
    void withdraw_이후_id_socialProvider_createdAt은_유지된다() {
        Member member = activeMember();
        Long originalId = member.getId();
        SocialProvider originalProvider = member.getSocialProvider();
        LocalDateTime originalCreatedAt = member.getCreatedAt();

        member.withdraw("withdrawn_uuid-5678");

        assertThat(member.getId()).isEqualTo(originalId);
        assertThat(member.getSocialProvider()).isEqualTo(originalProvider);
        assertThat(member.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void isWithdrawn_상태가_WITHDRAWN이면_true를_반환한다() {
        Member member = activeMember();
        assertThat(member.isWithdrawn()).isFalse();

        member.withdraw("withdrawn_uuid-9999");

        assertThat(member.isWithdrawn()).isTrue();
    }

    @Test
    void isActive_WITHDRAWN_상태에서는_false를_반환한다() {
        Member member = activeMember();

        member.withdraw("withdrawn_uuid-0000");

        assertThat(member.isActive()).isFalse();
    }
}
