package com.bangawo.group.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateGroupRequest {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    private String themeTagCode;

    private List<String> categoryLabels;

    private List<String> vibes;

    private Boolean reservable;

    private Boolean parking;
}
