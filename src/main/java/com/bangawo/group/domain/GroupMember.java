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
    private AttendanceStatus attendanceStatus;
    private LocalDateTime joinedAt;

    @Builder
    public GroupMember(Long id, Long groupId, Long memberId, GroupMemberRole role,
                       AttendanceStatus attendanceStatus, LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.role = role;
        this.attendanceStatus = attendanceStatus;
        this.joinedAt = joinedAt;
    }

    public static GroupMember createHost(Long groupId, Long memberId) {
        return GroupMember.builder()
                .groupId(groupId).memberId(memberId)
                .role(GroupMemberRole.HOST)
                .attendanceStatus(AttendanceStatus.JOIN)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public static GroupMember createMember(Long groupId, Long memberId) {
        return GroupMember.builder()
                .groupId(groupId).memberId(memberId)
                .role(GroupMemberRole.MEMBER)
                .attendanceStatus(AttendanceStatus.JOIN)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public void updateAttendance(AttendanceStatus status) {
        this.attendanceStatus = status;
    }
}
