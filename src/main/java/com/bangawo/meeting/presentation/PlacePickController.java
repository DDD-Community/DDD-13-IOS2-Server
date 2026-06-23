package com.bangawo.meeting.presentation;

import com.bangawo.meeting.application.PlacePickService;
import com.bangawo.meeting.presentation.dto.PickStatusResponse;
import com.bangawo.meeting.presentation.dto.PlaceCardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "장소 담기", description = "장소 후보 탐색 · 담기/취소 · 담기현황 · 투표 시작")
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class PlacePickController {

    private final PlacePickService placePickService;

    @Operation(summary = "장소 목록 조회 — 역탭·카테고리·예약·주차 필터, 추천 스코어 순")
    @GetMapping("/{meetingId}/places")
    public List<PlaceCardResponse> getPlaces(
            @PathVariable Long meetingId,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean reservable,
            @RequestParam(required = false) Boolean parking,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placePickService.getPlaces(meetingId, memberId, stationId, category, reservable, parking);
    }

    @Operation(summary = "장소 담기 — 멱등(이미 담은 경우 무시)")
    @PostMapping("/{meetingId}/places/{placeId}/pick")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickPlace(@PathVariable Long meetingId,
                          @PathVariable Long placeId,
                          Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placePickService.pickPlace(meetingId, memberId, placeId);
    }

    @Operation(summary = "장소 담기 취소 — 없는 담기 취소는 무시")
    @DeleteMapping("/{meetingId}/places/{placeId}/pick")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelPick(@PathVariable Long meetingId,
                           @PathVariable Long placeId,
                           Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        placePickService.cancelPick(meetingId, memberId, placeId);
    }

    @Operation(summary = "담기 현황 조회 — 모임원별 담기완료 여부 + 내가 담은 장소 목록")
    @GetMapping("/{meetingId}/places/pick-status")
    public PickStatusResponse getPickStatus(@PathVariable Long meetingId,
                                             Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return placePickService.getPickStatus(meetingId, memberId);
    }
}
