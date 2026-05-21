package com.bangawo.group.domain;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Group {

    private Long id;
    private String name;
    private String themeTagCode;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Group(Long id, String name, String themeTagCode, GroupStatus status,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.themeTagCode = themeTagCode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Group create(String name, String themeTagCode) {
        if (name == null || name.isBlank() || name.length() > 30) {
            throw new BusinessException(ErrorCode.GROUP_NAME_TOO_LONG);
        }
        return Group.builder()
                .name(name)
                .themeTagCode(themeTagCode)
                .status(GroupStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
