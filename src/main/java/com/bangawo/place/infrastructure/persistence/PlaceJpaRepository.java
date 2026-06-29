package com.bangawo.place.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceJpaRepository extends JpaRepository<PlaceJpaEntity, Long> {

    @Query(nativeQuery = true, value = """
            WITH stations AS (
                SELECT station_id, location_point
                FROM subway_station
                WHERE station_id IN (:stationIds)
            ),
            distances AS (
                SELECT
                    p.id AS place_id,
                    st.station_id AS station_id,
                    ST_DistanceSphere(p.location_point::geometry, st.location_point::geometry) AS dist_m
                FROM place p
                CROSS JOIN stations st
                WHERE p.location_point IS NOT NULL
            ),
            nearest AS (
                SELECT place_id, station_id, dist_m,
                       ROW_NUMBER() OVER (PARTITION BY place_id ORDER BY dist_m ASC) AS rn
                FROM distances
            )
            SELECT n.place_id, n.station_id
            FROM nearest n
            JOIN place p ON p.id = n.place_id
            WHERE n.rn = 1
              AND n.dist_m <= :radiusMeters
              AND (:reservable IS NOT TRUE OR p.reservable IS DISTINCT FROM FALSE)
              AND (:parking IS NOT TRUE OR p.has_parking IS DISTINCT FROM FALSE)
            """)
    List<Object[]> findCandidateIdsNearStations(@Param("stationIds") List<Long> stationIds,
                                                 @Param("radiusMeters") double radiusMeters,
                                                 @Param("reservable") Boolean reservable,
                                                 @Param("parking") Boolean parking);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT v
            FROM place, unnest(vibe) AS v
            WHERE v IS NOT NULL
            ORDER BY v
            """)
    List<String> findDistinctVibes();

    @Query(nativeQuery = true, value = """
            SELECT p.id AS place_id,
                   ST_DistanceSphere(
                       p.location_point::geometry,
                       ST_MakePoint(:longitude, :latitude)
                   ) AS distance_meters
            FROM place p
            WHERE ST_DWithin(
                p.location_point,
                ST_MakePoint(:longitude, :latitude)::geography,
                :radiusMeters
            )
            AND (:categoryLabel IS NULL OR p.category_label = :categoryLabel)
            ORDER BY distance_meters ASC
            LIMIT :limit
            """)
    List<Object[]> findNearbyRaw(@Param("latitude") double latitude,
                                 @Param("longitude") double longitude,
                                 @Param("radiusMeters") double radiusMeters,
                                 @Param("categoryLabel") String categoryLabel,
                                 @Param("limit") int limit);
}
