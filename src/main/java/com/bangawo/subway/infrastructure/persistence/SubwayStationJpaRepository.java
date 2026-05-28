package com.bangawo.subway.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

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
            candidates AS (
                SELECT
                    s.station_name,
                    string_agg(s.line_name, ', ' ORDER BY s.line_name) AS lines,
                    MIN(ST_DistanceSphere(
                        s.location_point::geometry,
                        c.geom::geometry
                    )) AS dist_m
                FROM subway_station s, center_point c
                WHERE ST_DWithin(s.location_point, c.geom, 2000)
                GROUP BY s.station_name
            )
            SELECT
                station_name,
                lines,
                ROUND(CAST(dist_m AS numeric) / 1000, 3) AS distance_km
            FROM candidates
            ORDER BY dist_m ASC
            LIMIT :limit
            """)
    List<Object[]> findRawCandidatesNearMeetingCenter(@Param("meetingId") Long meetingId,
                                                       @Param("limit") int limit);
}
