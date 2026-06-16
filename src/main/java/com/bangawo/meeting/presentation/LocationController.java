package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.PlaceSelectionService;
import com.bangawo.meeting.domain.MidpointStationCandidate;
import com.bangawo.meeting.presentation.dto.MidpointStationCandidateResponse;
import com.bangawo.meeting.presentation.dto.PlaceRecommendRequest;
import com.bangawo.meeting.presentation.dto.RecommendationItemResponse;
import com.bangawo.meeting.presentation.dto.UpdateParticipantDepartureRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "모임 장소", description = "장소 정하기 시작(중간역+추천) · 추천 조회 · 출발지 변경 · 중간지점 역 후보 조회")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class LocationController {

    private final PlaceSelectionService placeSelectionService;

    @Operation(summary = "장소 정하기 시작 — 호스트가 시작 시 중간역 3개 + 장소 추천 15개 산출, RECOMMENDED 전이")
    @PostMapping("/{meetingId}/location/start")
    @ResponseStatus(HttpStatus.OK)
    public void startLocationPhase(@PathVariable Long meetingId,
                                    @RequestBody(required = false) PlaceRecommendRequest request,
                                    Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        Double radiusKm = request != null ? request.radiusKm() : null;
        placeSelectionService.startLocationPhase(meetingId, memberId, radiusKm);
    }

    @Operation(summary = "장소 추천 결과 조회 — rank/placeId/name/categoryLabel/score/nearestStationId")
    @GetMapping("/{meetingId}/recommendations")
    public List<RecommendationItemResponse> getRecommendations(@PathVariable Long meetingId,
                                                                 Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placeSelectionService.getRecommendations(meetingId, memberId);
    }

    @Operation(summary = "모임 출발지 변경 — 내 meeting_participant 출발지를 선택한 departure_place로 업데이트")
    @PatchMapping("/{meetingId}/participants/me/departure")
    @ResponseStatus(HttpStatus.OK)
    public void updateParticipantDeparture(@PathVariable Long meetingId,
                                           @Valid @RequestBody UpdateParticipantDepartureRequest request,
                                           Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placeSelectionService.updateParticipantDeparture(meetingId, memberId, request.departurePlaceId());
    }

    @Operation(summary = "중간지점 역 후보 조회 — 장소 선정 단계 시작 후 rank 1~3 반환")
    @GetMapping("/{meetingId}/midpoint-stations")
    public MidpointStationCandidateResponse getMidpointStations(@PathVariable Long meetingId,
                                                                  Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        List<MidpointStationCandidate> candidates = placeSelectionService.getMidpointStations(meetingId, memberId);
        return MidpointStationCandidateResponse.from(candidates);
    }
}
