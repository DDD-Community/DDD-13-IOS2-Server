package com.bangawo.group.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateGroupRequest {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    private String themeTagCode;
}
