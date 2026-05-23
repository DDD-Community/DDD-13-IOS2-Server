package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.MeetingListService;
import com.bangawo.meeting.presentation.dto.MeetingCardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "모임", description = "모임 목록 조회 · 상세 · 날짜 투표")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingListService meetingListService;

    @Operation(
            summary = "내 모임 리스트 조회 — 소속 그룹의 최신 모임 카드 목록 반환",
            description = "진행 중 → 확정 → 종료 순 정렬. 동일 상태 내에서는 최신 모임이 위에 표시됩니다."
    )
    @GetMapping
    public List<MeetingCardResponse> getMyMeetings(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return meetingListService.getMyMeetingList(memberId);
    }
}
