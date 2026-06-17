package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.PlaceConfirmService;
import com.bangawo.meeting.application.PlaceVoteService;
import com.bangawo.meeting.presentation.dto.PlaceResultResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteStatusResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장소 투표", description = "투표 제출 · 재투표 · 투표현황 · 이동부담 · 확정결과 조회")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class PlaceVoteController {

    private final PlaceVoteService placeVoteService;
    private final PlaceConfirmService placeConfirmService;

    @Operation(summary = "장소 투표 제출 — 다중선택(후보 50% 내림, 최소1), 재투표 허용")
    @PostMapping("/{meetingId}/place-vote/submit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitVote(@PathVariable Long meetingId,
                            @Valid @RequestBody PlaceVoteSubmitRequest request,
                            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placeVoteService.submitVote(meetingId, memberId, request.placeIds());
    }

    @Operation(summary = "투표 현황 조회 — 후보별 득표수(익명), 이동부담, 모임원 투표완료 현황")
    @GetMapping("/{meetingId}/place-vote")
    public PlaceVoteStatusResponse getVoteStatus(@PathVariable Long meetingId,
                                                  Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeVoteService.getVoteStatus(meetingId, memberId);
    }

    @Operation(summary = "확정 장소 결과 조회 — CONFIRMED 상태 모임만, 득표수·이동부담 요약 포함")
    @GetMapping("/{meetingId}/place-result")
    public PlaceResultResponse getResult(@PathVariable Long meetingId,
                                          Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeConfirmService.getResult(meetingId, memberId);
    }
}
