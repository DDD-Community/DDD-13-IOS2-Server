package com.bangawo.group.presentation;

import com.bangawo.group.application.GroupMemberService;
import com.bangawo.group.presentation.dto.AttendanceUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그룹 구성원", description = "참석여부 수정")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @Operation(summary = "참석여부 수정", description = "본인의 참석여부를 JOIN / LATE / ABSENT 중 하나로 수정합니다")
    @PatchMapping("/{groupId}/members/me/attendance")
    public ResponseEntity<Void> updateAttendance(
            Authentication auth,
            @PathVariable Long groupId,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        Long memberId = (Long) auth.getPrincipal();
        groupMemberService.updateAttendance(memberId, groupId, request.attendanceStatus());
        return ResponseEntity.ok().build();
    }
}
