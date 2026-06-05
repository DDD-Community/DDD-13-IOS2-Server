package com.bangawo.subway.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subway_station")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubwayStationJpaEntity {

    @Id
    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;
}
