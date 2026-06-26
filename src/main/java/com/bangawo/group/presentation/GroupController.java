package com.bangawo.group.presentation;

import com.bangawo.group.application.GroupService;
import com.bangawo.group.presentation.dto.CreateGroupRequest;
import com.bangawo.group.presentation.dto.CreateGroupResponse;
import com.bangawo.group.presentation.dto.GroupMemberResponse;
import com.bangawo.meeting.presentation.dto.CreateMeetingRequest;
import com.bangawo.meeting.presentation.dto.CreateMeetingResponse;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

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
        return groupService.createGroupWithMeeting(memberId, request.getName(), request.getThemeTagCode(),
                request.getCategoryLabels(), request.getVibes(), request.getReservable(), request.getParking());
    }

    @Operation(summary = "그룹 종료 — 호스트가 그룹을 수동으로 종료 (호스트 전용)")
    @PatchMapping("/{groupId}/close")
    public ResponseEntity<Void> closeGroup(@PathVariable Long groupId, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        groupService.closeGroup(groupId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "새 모임 생성 — 종료된 그룹에서 다음 모임 생성 + 참여자 명단 선택 (호스트 전용)",
            description = "최신 모임이 CLOSED일 때만 생성 가능. body.participantMemberIds로 이번 모임 참여자를 선택한다 "
                    + "(GET /groups/{id}/members 로 후보 조회). 호스트는 명단에 없어도 자동 포함, 중복 제거. "
                    + "선택 멤버는 모두 현재 그룹 구성원이어야 함(아니면 403). 선택된 각 멤버는 meeting_participant로 "
                    + "시딩됨(참석여부 JOIN, 각자 기본 출발지 좌표; 없으면 null). 오류: 403 GROUP_004(호스트 아님)/"
                    + "403 GROUP_003(비구성원 포함), 404 MEETING_001(이전 모임 없음), 400 MEETING_009(미종료).")
    @PostMapping("/{groupId}/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateMeetingResponse createNextMeeting(@PathVariable Long groupId,
                                                   @Valid @RequestBody CreateMeetingRequest request,
                                                   Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        Long meetingId = groupService.createNextMeeting(groupId, memberId, request.name(), request.themeTagCode(),
                request.categoryLabels(), request.vibes(), request.reservable(), request.parking(),
                request.participantMemberIds());
        return new CreateMeetingResponse(meetingId);
    }

    @Operation(summary = "그룹 구성원 목록 조회 — 새 모임 생성 시 참여자 선택용 (구성원만 호출 가능)",
            description = "joinedAt 오름차순. 항목: memberId·nickname·profileImageUrl·role(HOST/MEMBER)·joinedAt. "
                    + "탈퇴 회원은 nickname/profileImageUrl이 null. 비구성원 호출 시 403 GROUP_003.")
    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> getGroupMembers(@PathVariable Long groupId, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return groupService.getGroupMembers(groupId, memberId);
    }
}
