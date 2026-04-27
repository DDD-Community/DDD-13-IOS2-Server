package com.bangawo.member.presentation;

import com.bangawo.member.application.DeparturePlaceService;
import com.bangawo.member.presentation.dto.DeparturePlaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "출발지", description = "출발지 관리")
@RestController
@RequestMapping("/api/v1/departure-places")
@RequiredArgsConstructor
public class DeparturePlaceController {

    private final DeparturePlaceService service;

    @Operation(summary = "출발지 전체 조회")
    @GetMapping
    public ResponseEntity<List<DeparturePlaceResponse>> getAll(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(
                service.getAll(memberId).stream()
                        .map(DeparturePlaceResponse::from).toList());
    }

    @Operation(summary = "출발지 삭제", description = "기본 출발지는 삭제 불가")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        Long memberId = (Long) auth.getPrincipal();
        service.delete(memberId, id);
        return ResponseEntity.ok().build();
    }
}
