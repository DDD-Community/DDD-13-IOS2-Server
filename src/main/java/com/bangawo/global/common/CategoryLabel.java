package com.bangawo.global.common;

import java.util.Set;

/**
 * 장소 추천용 음식 카테고리 — 고정 11종 (PRD 기준, DB 관리 아님).
 * meeting 생성 시 입력값 검증, place 추천 옵션(GET /places/options) 양쪽에서 공용으로 사용.
 */
public enum CategoryLabel {
    한식, 중식, 일식, 양식, 카페, 디저트, 주점, 분식, 아시아음식, 뷔페, 기타;

    private static final Set<String> NAMES = Set.of(
            한식.name(), 중식.name(), 일식.name(), 양식.name(), 카페.name(),
            디저트.name(), 주점.name(), 분식.name(), 아시아음식.name(), 뷔페.name(), 기타.name()
    );

    public static boolean isValid(String value) {
        return NAMES.contains(value);
    }
}
