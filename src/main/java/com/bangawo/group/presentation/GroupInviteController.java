package com.bangawo.group.presentation;

import com.bangawo.group.application.GroupInviteService;
import com.bangawo.group.presentation.dto.InviteCodeResponse;
import com.bangawo.group.presentation.dto.JoinGroupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그룹 초대", description = "초대 코드 발급 · 합류")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    @Operation(summary = "초대 코드 발급 — 호스트가 발급, 기존 코드는 무효화 후 재발급")
    @PostMapping("/{groupId}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteCodeResponse issueInviteCode(@PathVariable Long groupId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        String code = groupInviteService.issueInviteCode(groupId, memberId);
        return new InviteCodeResponse(code);
    }

    @Operation(summary = "초대 코드로 그룹 합류 — group_member 및 meeting_participant 자동 생성")
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public void joinGroup(@Valid @RequestBody JoinGroupRequest request, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        groupInviteService.joinGroup(request.inviteCode(), memberId);
    }
}
