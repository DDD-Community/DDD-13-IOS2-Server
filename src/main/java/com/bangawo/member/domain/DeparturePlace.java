package com.bangawo.member.domain;

import com.bangawo.global.common.Coordinate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 출발지 도메인 엔티티 */
@Getter
public class DeparturePlace {

    private Long id;
    private Long memberId;
    private String label;        // "집", "회사" 등
    private String address;      // 주소 원문
    private Coordinate coordinate;
    private boolean isDefault;   // 기본 출발지 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public DeparturePlace(Long id, Long memberId, String label, String address,
                          Coordinate coordinate, boolean isDefault,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.label = label;
        this.address = address;
        this.coordinate = coordinate;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(String label, String address, Coordinate coordinate) {
        this.label = label;
        this.address = address;
        this.coordinate = coordinate;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
