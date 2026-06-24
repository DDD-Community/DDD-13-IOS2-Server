package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.PlaceConfirmService;
import com.bangawo.meeting.application.PlaceVoteService;
import com.bangawo.meeting.presentation.dto.PlaceResultResponse;
import com.bangawo.meeting.presentation.dto.PlaceTravelBurdenResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteStatusResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteSubmitRequest;
import com.bangawo.meeting.presentation.dto.StartPlaceVoteRequest;
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

    @Operation(summary = "투표 시작 — 호스트 전용, 후보 ≥1 필요, VOTING 전환 + 마감일 설정")
    @PostMapping("/{meetingId}/place-vote")
    @ResponseStatus(HttpStatus.OK)
    public void startVoting(@PathVariable Long meetingId,
                            @Valid @RequestBody StartPlaceVoteRequest request,
                            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placeVoteService.startVoting(meetingId, memberId, request.durationDays());
    }

    @Operation(summary = "장소 투표 제출 — 다중선택(후보 50% 내림, 최소1), 재투표 허용")
    @PostMapping("/{meetingId}/place-vote/submit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitVote(@PathVariable Long meetingId,
                            @Valid @RequestBody PlaceVoteSubmitRequest request,
                            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placeVoteService.submitVote(meetingId, memberId, request.placeIds());
    }

    @Operation(summary = "투표 현황 조회 — 후보별 득표수(익명) · 이동부담 · 모임원별 투표완료 현황(전원 공개)",
            description = "후보=담긴 장소(백필 포함). 정렬: 미투표 시 가나다순 / 투표 후 득표순. memberStatuses는 호스트·구성원 모두에게 제공(완료여부만, 투표 대상은 비공개)")
    @GetMapping("/{meetingId}/place-vote")
    public PlaceVoteStatusResponse getVoteStatus(@PathVariable Long meetingId,
                                                  Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeVoteService.getVoteStatus(meetingId, memberId);
    }

    @Operation(summary = "친구들 거리보기 — 특정 장소 1건에 대한 모임원별 소요시간·환승 조회",
            description = "장소 상세 화면 '친구들 거리보기' 버튼용. 투표 시작 시 저장된 이동부담 스냅샷 기반(신규 계산 없음)")
    @GetMapping("/{meetingId}/place-vote/{placeId}/travel-burden")
    public PlaceTravelBurdenResponse getPlaceTravelBurden(@PathVariable Long meetingId,
                                                          @PathVariable Long placeId,
                                                          Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeVoteService.getPlaceTravelBurden(meetingId, placeId, memberId);
    }

    @Operation(summary = "호스트 수동 확정 — VOTING 상태에서 현재 순위 1위로 즉시 확정 → CONFIRMED 전환",
            description = "호스트 전용. 자동 확정(전원완료/마감)과 동일한 순위 비교자로 1위를 선정한다")
    @PostMapping("/{meetingId}/place-confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmByHost(@PathVariable Long meetingId,
                              Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placeConfirmService.confirmByHost(meetingId, memberId);
    }

    @Operation(summary = "확정 장소 결과 조회 — CONFIRMED 상태 모임만, 득표수·이동부담 요약 포함")
    @GetMapping("/{meetingId}/place-result")
    public PlaceResultResponse getResult(@PathVariable Long meetingId,
                                          Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeConfirmService.getResult(meetingId, memberId);
    }
}
