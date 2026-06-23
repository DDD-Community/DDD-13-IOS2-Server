package com.bangawo.subway.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubwayStationJpaRepository extends JpaRepository<SubwayStationJpaEntity, Long> {

    @Query(nativeQuery = true, value = """
            WITH center_point AS (
                SELECT ST_Centroid(
                    ST_Collect(
                        ST_SetSRID(ST_MakePoint(mp.longitude, mp.latitude), 4326)::geometry
                    )
                )::geography AS geom
                FROM meeting_participant mp
                WHERE mp.meeting_id = :meetingId
                  AND mp.attendance_status != 'ABSENT'
            ),
            ladder AS (
                SELECT unnest(ARRAY[2000, 4000, 6000]) AS radius
            ),
            chosen_radius AS (
                SELECT MIN(l.radius) AS radius
                FROM ladder l, center_point c
                WHERE EXISTS (
                    SELECT 1 FROM subway_station s
                    WHERE ST_DWithin(s.location_point, c.geom, l.radius)
                )
            ),
            candidates AS (
                SELECT
                    s.station_name,
                    string_agg(s.line_name, ', ' ORDER BY s.line_name) AS lines,
                    MIN(ST_DistanceSphere(
                        s.location_point::geometry,
                        c.geom::geometry
                    )) AS dist_m,
                    MIN(s.station_id) AS station_id,
                    MIN(s.latitude) AS latitude,
                    MIN(s.longitude) AS longitude
                FROM subway_station s, center_point c, chosen_radius r
                WHERE r.radius IS NOT NULL
                  AND ST_DWithin(s.location_point, c.geom, r.radius)
                GROUP BY s.station_name
            )
            SELECT
                station_name,
                lines,
                ROUND(CAST(dist_m AS numeric) / 1000, 3) AS distance_km,
                station_id,
                latitude,
                longitude
            FROM candidates
            ORDER BY dist_m ASC
            LIMIT :limit
            """)
    List<Object[]> findRawCandidatesNearMeetingCenter(@Param("meetingId") Long meetingId,
                                                       @Param("limit") int limit);

    @Query(nativeQuery = true, value = """
            SELECT s.station_id
            FROM subway_station s
            ORDER BY ST_DistanceSphere(
                s.location_point::geometry,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geometry
            )
            LIMIT 1
            """)
    Optional<Long> findNearestStationId(@Param("latitude") double latitude,
                                        @Param("longitude") double longitude);
}
