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
    private String label;
    private String address;       // 지번 주소
    private String roadAddress;   // 도로명 주소
    private String placeName;     // 카카오 장소명 (nullable)
    private Coordinate coordinate;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public DeparturePlace(Long id, Long memberId, String label, String address,
                          String roadAddress, String placeName,
                          Coordinate coordinate, boolean isDefault,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.label = label;
        this.address = address;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
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

    public void update(String label, String address, String roadAddress, String placeName,
                       double latitude, double longitude) {
        this.label = label;
        this.address = address;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
        this.coordinate = new Coordinate(latitude, longitude);
        this.updatedAt = LocalDateTime.now();
    }
}
