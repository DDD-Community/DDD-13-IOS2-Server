package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.LocationService;
import com.bangawo.meeting.domain.MidpointStationCandidate;
import com.bangawo.meeting.presentation.dto.MidpointStationCandidateResponse;
import com.bangawo.meeting.presentation.dto.UpdateParticipantDepartureRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "모임 장소", description = "장소 선정 단계 시작 · 모임 출발지 변경 · 중간지점 역 후보 조회")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "장소 선정 단계 시작 — 호스트가 시작 시 중간지점 역 3개 자동 계산·저장")
    @PostMapping("/{meetingId}/location/start")
    @ResponseStatus(HttpStatus.OK)
    public void startLocationPhase(@PathVariable Long meetingId, Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        locationService.startLocationPhase(meetingId, memberId);
    }

    @Operation(summary = "모임 출발지 변경 — 내 meeting_participant 출발지를 선택한 departure_place로 업데이트")
    @PatchMapping("/{meetingId}/participants/me/departure")
    @ResponseStatus(HttpStatus.OK)
    public void updateParticipantDeparture(@PathVariable Long meetingId,
                                           @Valid @RequestBody UpdateParticipantDepartureRequest request,
                                           Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        locationService.updateParticipantDeparture(meetingId, memberId, request.departurePlaceId());
    }

    @Operation(summary = "중간지점 역 후보 조회 — 장소 선정 단계 시작 후 rank 1~3 반환")
    @GetMapping("/{meetingId}/midpoint-stations")
    public MidpointStationCandidateResponse getMidpointStations(@PathVariable Long meetingId,
                                                                  Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        List<MidpointStationCandidate> candidates = locationService.getMidpointStations(meetingId, memberId);
        return MidpointStationCandidateResponse.from(candidates);
    }
}
