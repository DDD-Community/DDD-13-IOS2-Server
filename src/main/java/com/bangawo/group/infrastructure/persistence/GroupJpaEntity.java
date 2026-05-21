package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "theme_tag_code", nullable = false, length = 30)
    private String themeTagCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static GroupJpaEntity from(Group group) {
        GroupJpaEntity entity = new GroupJpaEntity();
        entity.id = group.getId();
        entity.name = group.getName();
        entity.themeTagCode = group.getThemeTagCode();
        entity.status = group.getStatus();
        entity.createdAt = group.getCreatedAt();
        entity.updatedAt = group.getUpdatedAt();
        return entity;
    }

    public Group toDomain() {
        return Group.builder()
                .id(id)
                .name(name)
                .themeTagCode(themeTagCode)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
