package com.bangawo.group.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ThemeTag {
    private final Long id;
    private final String code;
    private final String displayName;
    private final int sortOrder;
    private final boolean active;

    @Builder
    public ThemeTag(Long id, String code, String displayName, int sortOrder, boolean active) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.sortOrder = sortOrder;
        this.active = active;
    }
}
