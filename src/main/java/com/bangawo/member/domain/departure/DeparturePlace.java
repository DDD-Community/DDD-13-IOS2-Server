package com.bangawo.member.domain.departure;

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
    private String address;
    private Coordinate coordinate;
    private boolean isDefault;
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

    public void clearDefault() {
        this.isDefault = false;
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public void update(String label, String address, double latitude, double longitude) {
        this.label = label;
        this.address = address;
        this.coordinate = new Coordinate(latitude, longitude);
        this.updatedAt = LocalDateTime.now();
    }
}
