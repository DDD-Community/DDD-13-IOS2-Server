package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GroupMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 10)
    private AttendanceStatus attendanceStatus;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public static GroupMemberJpaEntity from(GroupMember groupMember) {
        GroupMemberJpaEntity entity = new GroupMemberJpaEntity();
        entity.id = groupMember.getId();
        entity.groupId = groupMember.getGroupId();
        entity.memberId = groupMember.getMemberId();
        entity.role = groupMember.getRole();
        entity.attendanceStatus = groupMember.getAttendanceStatus();
        entity.joinedAt = groupMember.getJoinedAt();
        return entity;
    }

    public GroupMember toDomain() {
        return GroupMember.builder()
                .id(id)
                .groupId(groupId)
                .memberId(memberId)
                .role(role)
                .attendanceStatus(attendanceStatus)
                .joinedAt(joinedAt)
                .build();
    }
}
