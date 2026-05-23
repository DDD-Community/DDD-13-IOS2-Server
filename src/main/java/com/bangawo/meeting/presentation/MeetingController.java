package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.DateVoteService;
import com.bangawo.meeting.application.MeetingDetailService;
import com.bangawo.meeting.application.MeetingListService;
import com.bangawo.meeting.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "모임", description = "모임 목록 조회 · 상세 · 날짜 투표")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingListService meetingListService;
    private final MeetingDetailService meetingDetailService;
    private final DateVoteService dateVoteService;

    @Operation(summary = "내 모임 리스트 조회")
    @GetMapping
    public List<MeetingCardResponse> getMyMeetings(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return meetingListService.getMyMeetingList(memberId);
    }

    @Operation(summary = "모임 상세 조회")
    @GetMapping("/{meetingId}")
    public MeetingDetailResponse getDetail(@PathVariable Long meetingId, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return meetingDetailService.getDetail(meetingId, memberId);
    }

    @Operation(summary = "날짜 투표 — 방식 A: 호스트 단독 선택 즉시 확정")
    @PostMapping("/{meetingId}/date-vote/host-pick")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hostPick(@PathVariable Long meetingId,
                         @RequestBody @Valid HostPickRequest request,
                         Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        dateVoteService.startHostPick(meetingId, memberId, request.date());
    }

    @Operation(summary = "날짜 투표 — 방식 B: 투표 시작")
    @PostMapping("/{meetingId}/date-vote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startVote(@PathVariable Long meetingId,
                          @RequestBody @Valid StartVoteRequest request,
                          Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        dateVoteService.startVote(meetingId, memberId, request);
    }

    @Operation(summary = "날짜 투표 참여")
    @PostMapping("/{meetingId}/date-vote/submit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitVote(@PathVariable Long meetingId,
                           @RequestBody @Valid SubmitVoteRequest request,
                           Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        dateVoteService.submitVote(meetingId, memberId, request);
    }

    @Operation(summary = "날짜 투표 현황 조회")
    @GetMapping("/{meetingId}/date-vote")
    public VoteStatusResponse getVoteStatus(@PathVariable Long meetingId, Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return dateVoteService.getVoteStatus(meetingId, memberId);
    }

    @Operation(summary = "호스트 수동 날짜 확정")
    @PatchMapping("/{meetingId}/date-vote/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmDate(@PathVariable Long meetingId,
                            @RequestBody @Valid ConfirmDateRequest request,
                            Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        dateVoteService.confirmDate(meetingId, memberId, request);
    }
}
