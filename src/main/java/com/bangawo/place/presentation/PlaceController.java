package com.bangawo.place.presentation;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.place.domain.PlaceOption;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.presentation.dto.PlaceDetailResponse;
import com.bangawo.place.presentation.dto.PlaceOptionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "장소", description = "모임 생성/장소 추천 화면용 선택지 조회")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceRepository placeRepository;

    @Operation(summary = "장소 추천 선택지 조회 — 카테고리 고정 11종 + vibe 표준목록(place.vibe distinct)")
    @GetMapping("/options")
    public PlaceOptionsResponse getOptions() {
        return new PlaceOptionsResponse(PlaceOption.categories(), placeRepository.findDistinctVibes());
    }

    @Operation(summary = "장소 상세 조회 — placeId로 이름·주소·좌표·vibe·예약/주차·평점 등 단건 조회")
    @GetMapping("/{placeId}")
    public PlaceDetailResponse getPlace(@PathVariable Long placeId) {
        return placeRepository.findById(placeId)
                .map(PlaceDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }
}
