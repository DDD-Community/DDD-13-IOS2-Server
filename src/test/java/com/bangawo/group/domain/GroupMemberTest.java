package com.bangawo.group.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMemberTest {

    @Test
    void promoteToHost_역할을_HOST로_전환한다() {
        GroupMember member = GroupMember.createMember(1L, 10L);
        assertThat(member.getRole()).isEqualTo(GroupMemberRole.MEMBER);

        member.promoteToHost();

        assertThat(member.getRole()).isEqualTo(GroupMemberRole.HOST);
    }
}
