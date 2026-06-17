package com.bangawo.place.domain;

import com.bangawo.global.common.CategoryLabel;

import java.util.Arrays;
import java.util.List;

/**
 * 모임 생성/장소 추천 화면에서 제공하는 선택지. 카테고리는 고정 11종(하드코딩), vibe는 PlaceRepository에서 동적 조회.
 */
public class PlaceOption {

    public static List<String> categories() {
        return Arrays.stream(CategoryLabel.values()).map(Enum::name).toList();
    }
}
