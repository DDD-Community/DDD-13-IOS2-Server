package com.bangawo.place.infrastructure.persistence;

import com.bangawo.place.domain.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private String name;

    @Column(name = "category_label")
    private String categoryLabel;

    @Column
    private String address;

    private Double latitude;

    private Double longitude;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> vibe;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> occasion;

    @Column
    private Boolean reservable;

    @Column(name = "has_parking")
    private Boolean hasParking;

    @Column
    private java.math.BigDecimal rating;

    public Place toDomain() {
        return Place.builder()
                .id(id)
                .placeId(placeId)
                .name(name)
                .categoryLabel(categoryLabel)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .vibe(vibe)
                .occasion(occasion)
                .reservable(reservable)
                .hasParking(hasParking)
                .rating(rating != null ? rating.doubleValue() : null)
                .build();
    }
}
