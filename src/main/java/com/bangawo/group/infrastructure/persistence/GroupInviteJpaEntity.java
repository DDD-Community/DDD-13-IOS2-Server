package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.GroupInvite;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_invite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupInviteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false, length = 36)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static GroupInviteJpaEntity from(GroupInvite domain) {
        GroupInviteJpaEntity e = new GroupInviteJpaEntity();
        e.id = domain.getId();
        e.groupId = domain.getGroupId();
        e.code = domain.getCode();
        e.expiresAt = domain.getExpiresAt();
        e.createdAt = domain.getCreatedAt();
        return e;
    }

    public GroupInvite toDomain() {
        return GroupInvite.builder()
                .id(id).groupId(groupId).code(code)
                .expiresAt(expiresAt).createdAt(createdAt)
                .build();
    }
}
