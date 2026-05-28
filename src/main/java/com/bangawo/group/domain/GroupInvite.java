package com.bangawo.group.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupInvite {

    private Long id;
    private Long groupId;
    private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    @Builder
    public GroupInvite(Long id, Long groupId, String code,
                       LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.code = code;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static GroupInvite create(Long groupId, String code, LocalDateTime expiresAt) {
        return GroupInvite.builder()
                .groupId(groupId)
                .code(code)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
