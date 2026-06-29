package com.bangawo.place.presentation;

import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceOption;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.presentation.dto.PlaceDetailResponse;
import com.bangawo.place.presentation.dto.PlaceNearbyResponse;
import com.bangawo.place.presentation.dto.PlaceOptionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Operation(summary = "장소 상세 조회 — placeId 1~N건 일괄 조회 (이름·주소·좌표·vibe·예약/주차·평점 등). 요청 순서 보존, 미존재 id는 제외")
    @GetMapping
    public List<PlaceDetailResponse> getPlaces(@RequestParam("ids") List<Long> ids) {
        Map<Long, Place> placeById = placeRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(Place::getId, Function.identity()));
        return ids.stream()
                .map(placeById::get)
                .filter(Objects::nonNull)
                .map(PlaceDetailResponse::from)
                .toList();
    }

    private static final int NEARBY_LIMIT = 50;

    @Operation(summary = "반경 기반 주변 장소 검색 — 기준 좌표·반경(미터) 기준 거리순, 최대 50건. category 필터 선택")
    @GetMapping("/nearby")
    public List<PlaceNearbyResponse> getNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radiusMeters,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        return placeRepository.findNearby(latitude, longitude, radiusMeters, category, NEARBY_LIMIT)
                .stream()
                .map(PlaceNearbyResponse::from)
                .toList();
    }
}
