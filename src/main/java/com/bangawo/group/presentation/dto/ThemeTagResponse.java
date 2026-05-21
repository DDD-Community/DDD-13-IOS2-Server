package com.bangawo.group.presentation.dto;

import com.bangawo.group.domain.ThemeTag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ThemeTagResponse {
    private String code;
    private String displayName;

    public static ThemeTagResponse from(ThemeTag themeTag) {
        return new ThemeTagResponse(themeTag.getCode(), themeTag.getDisplayName());
    }
}
