package com.bangawo.member.infrastructure.persistence.departure;

import com.bangawo.global.common.Coordinate;
import com.bangawo.member.domain.departure.DeparturePlace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "departure_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeparturePlaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 10)
    private String label;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static DeparturePlaceJpaEntity from(DeparturePlace domain) {
        DeparturePlaceJpaEntity e = new DeparturePlaceJpaEntity();
        e.id = domain.getId();
        e.memberId = domain.getMemberId();
        e.label = domain.getLabel();
        e.address = domain.getAddress();
        e.latitude = domain.getCoordinate().getLatitude();
        e.longitude = domain.getCoordinate().getLongitude();
        e.isDefault = domain.isDefault();
        e.createdAt = domain.getCreatedAt();
        e.updatedAt = domain.getUpdatedAt();
        return e;
    }

    public DeparturePlace toDomain() {
        return DeparturePlace.builder()
                .id(id)
                .memberId(memberId)
                .label(label)
                .address(address)
                .coordinate(new Coordinate(latitude, longitude))
                .isDefault(isDefault)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
