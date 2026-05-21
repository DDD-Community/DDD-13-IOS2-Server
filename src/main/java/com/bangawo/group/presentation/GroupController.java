package com.bangawo.group.presentation;

import com.bangawo.group.application.GroupService;
import com.bangawo.group.presentation.dto.CreateGroupRequest;
import com.bangawo.group.presentation.dto.CreateGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그룹", description = "그룹 생성 · 초대 코드 발급 · 구성원 관리")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(
            summary = "그룹 & 첫 모임 생성 — 이름·테마 입력 시 그룹/모임/호스트 멤버십 동시 생성",
            description = "모임 이름과 테마 태그를 입력하면 그룹 · 첫 번째 모임 · 호스트 멤버십이 한 번에 생성됩니다"
    )
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGroupResponse createGroup(Authentication auth,
                                           @Valid @RequestBody CreateGroupRequest request) {
        Long memberId = (Long) auth.getPrincipal();
        return groupService.createGroupWithMeeting(memberId, request.getName(), request.getThemeTagCode());
    }
}
