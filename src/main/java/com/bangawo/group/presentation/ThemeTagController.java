package com.bangawo.group.presentation;

import com.bangawo.group.application.GroupService;
import com.bangawo.group.presentation.dto.ThemeTagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "테마 태그", description = "그룹 테마 태그 목록 조회")
@RestController
@RequestMapping("/api/v1/theme-tags")
@RequiredArgsConstructor
public class ThemeTagController {

    private final GroupService groupService;

    @Operation(
            summary = "테마 태그 목록 조회 — 그룹 생성 시 선택 가능한 태그를 정렬 순서대로 반환",
            description = "그룹 생성 화면에서 선택 가능한 테마 태그 목록을 정렬 순서대로 반환합니다"
    )
    @GetMapping
    public List<ThemeTagResponse> getThemeTags() {
        return groupService.getActiveThemeTags();
    }
}
