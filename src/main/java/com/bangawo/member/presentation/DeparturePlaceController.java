package com.bangawo.member.presentation;

import com.bangawo.member.application.DeparturePlaceService;
import com.bangawo.member.presentation.dto.DeparturePlaceRequest;
import com.bangawo.member.presentation.dto.DeparturePlaceResponse;
import com.bangawo.member.presentation.dto.DeparturePlaceUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "출발지 추가", description = "최대 3개. 첫 등록 시 isDefault가 자동으로 true로 설정됩니다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeparturePlaceResponse create(Authentication auth,
                                         @Valid @RequestBody DeparturePlaceRequest request) {
        Long memberId = (Long) auth.getPrincipal();
        return DeparturePlaceResponse.from(
                service.create(memberId, request.label(), request.address(),
                        request.latitude(), request.longitude(), request.isDefault()));
    }

    @Operation(summary = "출발지 수정", description = "label / address / 좌표 수정 가능")
    @PutMapping("/{id}")
    public ResponseEntity<DeparturePlaceResponse> update(Authentication auth,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody DeparturePlaceUpdateRequest request) {
        Long memberId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(DeparturePlaceResponse.from(
                service.update(memberId, id, request.label(), request.address(),
                        request.latitude(), request.longitude())));
    }

    @Operation(summary = "기본 출발지 설정", description = "해당 출발지를 기본으로 설정하고 기존 기본 출발지를 해제합니다")
    @PatchMapping("/{id}/default")
    public ResponseEntity<DeparturePlaceResponse> setDefault(Authentication auth,
                                                              @PathVariable Long id) {
        Long memberId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(DeparturePlaceResponse.from(service.setDefault(memberId, id)));
    }

    @Operation(summary = "출발지 삭제", description = "기본 출발지는 삭제 불가")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        Long memberId = (Long) auth.getPrincipal();
        service.delete(memberId, id);
        return ResponseEntity.ok().build();
    }
}
