package com.all4land.parkinglotnavigator.parkinglot.repository;

import com.all4land.parkinglotnavigator.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    @Query(value = """
            SELECT
                p.id AS id,
                p.name AS name,
                p.road_name_address AS roadNameAddress,
                p.total_spaces AS totalSpaces,
                ST_Y(l.location::geometry) AS latitude,
                ST_X(l.location::geometry) AS longitude,
                ST_Distance(
                    l.location,
                    ST_SetSRID(
                        ST_MakePoint(:longitude, :latitude),
                        4326
                    )::geography
                ) AS distance
            FROM tb_parking_lot p
            JOIN tb_location l
              ON p.location_id = l.id
            WHERE ST_DWithin(
                l.location,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
                    4326
                )::geography,
                :radius
            )
              AND p.deleted_at IS NULL
            ORDER BY distance ASC
            """,
        nativeQuery = true)

    List<NearbyParkingLotProjection> findNearbyParkingLots(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radius") Double radius
    );
}
