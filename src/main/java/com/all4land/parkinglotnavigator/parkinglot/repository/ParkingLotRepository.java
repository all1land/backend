package com.all4land.parkinglotnavigator.parkinglot.repository;

import com.all4land.parkinglotnavigator.parkinglot.entity.ParkingLot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    @Query(value = """
        SELECT
            p.id AS id,
            p.name AS name,
            p.road_name_address AS roadNameAddress,
            p.phone AS phone,
            p.lot_number_address AS lotNumberAddress,

            p.category_code AS categoryCode,
            category.name AS categoryName,

            p.parking_type_code AS parkingTypeCode,
            parking_type.name AS parkingTypeName,

            p.total_spaces AS totalSpaces,

            ST_Y(p.location::geometry) AS latitude,
            ST_X(p.location::geometry) AS longitude,

            ST_Distance(
                p.location,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
                    4326
                )::geography
            ) AS distance
        FROM tb_parking_lot p

        LEFT JOIN tb_cmn_code category_root
            ON category_root.parent_id IS NULL
           AND category_root.code = 'PARKING_CATEGORY'

        LEFT JOIN tb_cmn_code category
            ON category.parent_id = category_root.id
           AND category.code = p.category_code
           AND category.state = 'ACTIVE'

        LEFT JOIN tb_cmn_code parking_type_root
            ON parking_type_root.parent_id IS NULL
           AND parking_type_root.code = 'PARKING_TYPE'

        LEFT JOIN tb_cmn_code parking_type
            ON parking_type.parent_id = parking_type_root.id
           AND parking_type.code = p.parking_type_code
           AND parking_type.state = 'ACTIVE'

        WHERE ST_DWithin(
            p.location,
            ST_SetSRID(
                ST_MakePoint(:longitude, :latitude),
                4326
            )::geography,
            :radius
        )
          AND p.deleted_at IS NULL
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<NearbyParkingLotProjection> findNearbyParkingLots(
            @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("radius") Double radius);

    @Query(value = """
        SELECT
            p.id AS id,
            p.name AS name,

            p.category_code AS categoryCode,
            category.name AS categoryName,

            p.parking_type_code AS parkingTypeCode,
            parking_type.name AS parkingTypeName,

            p.road_name_address AS roadNameAddress,
            p.lot_number_address AS lotNumberAddress,
            p.phone AS phone,
            p.total_spaces AS totalSpaces,

            p.operation_days AS operationDays,
            p.weekday_open AS weekdayOpen,
            p.weekday_close AS weekdayClose,
            p.saturday_open AS saturdayOpen,
            p.saturday_close AS saturdayClose,
            p.holiday_open AS holidayOpen,
            p.holiday_close AS holidayClose,

            p.fee_info AS feeInfo,
            p.base_minutes AS baseMinutes,
            p.base_fee AS baseFee,
            p.extra_unit_minutes AS extraUnitMinutes,
            p.extra_unit_fee AS extraUnitFee,
            p.daily_ticket_hours AS dailyTicketHours,
            p.daily_ticket_fee AS dailyTicketFee,
            p.monthly_pass_fee AS monthlyPassFee,

            p.payment_method AS paymentMethod,
            p.special_notes AS specialNotes,
            p.management_agency AS managementAgency,
            p.disabled_parking_yn AS disabledParkingYn,

            ST_Y(p.location::geometry) AS latitude,
            ST_X(p.location::geometry) AS longitude

        FROM tb_parking_lot p

        LEFT JOIN tb_cmn_code category_root
          ON category_root.parent_id IS NULL
         AND category_root.code = 'PARKING_CATEGORY'

        LEFT JOIN tb_cmn_code category
          ON category.parent_id = category_root.id
         AND category.code = p.category_code
         AND category.state = 'ACTIVE'

        LEFT JOIN tb_cmn_code parking_type_root
          ON parking_type_root.parent_id IS NULL
         AND parking_type_root.code = 'PARKING_TYPE'

        LEFT JOIN tb_cmn_code parking_type
          ON parking_type.parent_id = parking_type_root.id
         AND parking_type.code = p.parking_type_code
         AND parking_type.state = 'ACTIVE'

        WHERE p.id = :parkingId
          AND p.deleted_at IS NULL
        """, nativeQuery = true)
    Optional<ParkingLotDetailProjection> findParkingLotDetail(@Param("parkingId") Long parkingId);
}
