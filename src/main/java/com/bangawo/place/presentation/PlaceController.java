package com.bangawo.place.presentation;

import com.bangawo.place.domain.PlaceOption;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.presentation.dto.PlaceOptionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
