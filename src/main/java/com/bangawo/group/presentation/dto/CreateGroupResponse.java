package com.bangawo.group.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateGroupResponse {
    private Long groupId;
    private Long meetingId;
    private String name;
    private String themeTagCode;
}
