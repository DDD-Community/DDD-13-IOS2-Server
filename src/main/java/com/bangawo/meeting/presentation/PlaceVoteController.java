package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.PlaceConfirmService;
import com.bangawo.meeting.application.PlaceVoteService;
import com.bangawo.meeting.presentation.dto.PlaceResultResponse;
import com.bangawo.meeting.presentation.dto.PlaceTravelBurdenResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteStatusResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteSubmitRequest;
import com.bangawo.meeting.presentation.dto.StartPlaceVoteRequest;
import com.bangawo.meeting.presentation.dto.VoteParticipantsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장소 투표", description = "투표 시작 · 제출/재투표 · 투표현황 · 참여 팀원 · 친구들 거리보기 · 수동확정 · 확정결과")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class PlaceVoteController {

    private final PlaceVoteService placeVoteService;
    private final PlaceConfirmService placeConfirmService;

    @Operation(summary = "투표 시작 — 호스트 전용, VOTING 전환 + 마감일 설정",
            description = "후보 = 담긴 장소(pick). 후보가 3개 미만이면 추천 상위 순위로 자동 백필해 최소 3개 보장. 마감 기간 1/3/7일. 시작 시 멤버×후보 이동부담 스냅샷 계산·저장")
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

    @Operation(summary = "투표 현황 조회 — 후보별 득표수(익명) · 모임원별 투표완료 현황(전원 공개)",
            description = "후보=담긴 장소(백필 포함). 정렬: 미투표 시 가나다순 / 투표 후 득표순. memberStatuses는 호스트·구성원 모두에게 제공(완료여부만, 투표 대상은 비공개). "
                    + "이동부담은 PRD 12-3 범위 밖 — 친구들 거리보기 API(/{placeId}/travel-burden)에서 별도 제공")
    @GetMapping("/{meetingId}/place-vote")
    public PlaceVoteStatusResponse getVoteStatus(@PathVariable Long meetingId,
                                                  Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeVoteService.getVoteStatus(meetingId, memberId);
    }

    @Operation(summary = "친구들 거리보기 — 특정 장소 1건에 대한 활성 참여자별 소요시간·환승·경로 조회",
            description = "장소 상세 '친구들 거리보기' 버튼용. 투표 시작 시 저장된 이동부담 스냅샷 기반(신규 계산 없음). "
                    + "활성 참여자(ABSENT 제외) 전원 포함(스냅샷 없으면 seconds/transfers=null, path=[]). "
                    + "멤버별: name·departureName(저장 출발지명)·isMe(본인여부)·seconds·transfers·isLongest(최장이동자)·path(출발→도착 역 좌표)")
    @GetMapping("/{meetingId}/place-vote/{placeId}/travel-burden")
    public PlaceTravelBurdenResponse getPlaceTravelBurden(@PathVariable Long meetingId,
                                                          @PathVariable Long placeId,
                                                          Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeVoteService.getPlaceTravelBurden(meetingId, placeId, memberId);
    }

    @Operation(summary = "장소투표 참여 팀원 조회 — VOTING 상태, 활성 참여자별 이름·프로필·출발지·투표여부",
            description = "활성 참여자(ABSENT 제외) 전원. departureName=참여 당시 저장된 출발지명, voted=현재 세션 제출 여부, profileImageUrl=원본 object key")
    @GetMapping("/{meetingId}/place-vote/participants")
    public VoteParticipantsResponse getVoteParticipants(@PathVariable Long meetingId,
                                                        Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeVoteService.getVoteParticipants(meetingId, memberId);
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
