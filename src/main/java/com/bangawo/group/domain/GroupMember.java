package com.bangawo.group.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupMember {

    private Long id;
    private Long groupId;
    private Long memberId;
    private GroupMemberRole role;
    private LocalDateTime joinedAt;

    @Builder
    public GroupMember(Long id, Long groupId, Long memberId, GroupMemberRole role,
                       LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static GroupMember createHost(Long groupId, Long memberId) {
        return GroupMember.builder()
                .groupId(groupId).memberId(memberId)
                .role(GroupMemberRole.HOST)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public static GroupMember createMember(Long groupId, Long memberId) {
        return GroupMember.builder()
                .groupId(groupId).memberId(memberId)
                .role(GroupMemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    /** 호스트 승계 — 역할을 HOST로 전환 */
    public void promoteToHost() {
        this.role = GroupMemberRole.HOST;
    }
}
