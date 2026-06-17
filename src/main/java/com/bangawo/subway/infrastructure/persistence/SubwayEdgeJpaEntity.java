package com.bangawo.subway.infrastructure.persistence;

import com.bangawo.subway.domain.SubwayEdge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subway_edge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubwayEdgeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_station_id", nullable = false)
    private Long fromStationId;

    @Column(name = "to_station_id", nullable = false)
    private Long toStationId;

    @Column(name = "weight_sec", nullable = false)
    private int weightSec;

    @Column(name = "edge_type", nullable = false, length = 10)
    private String edgeType;

    public SubwayEdge toDomain() {
        return new SubwayEdge(fromStationId, toStationId, weightSec, edgeType);
    }
}
