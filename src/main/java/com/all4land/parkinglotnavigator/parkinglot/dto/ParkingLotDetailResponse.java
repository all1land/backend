package com.all4land.parkinglotnavigator.parkinglot.dto;

import com.all4land.parkinglotnavigator.parkinglot.entity.ParkingLot;

import java.time.LocalTime;

public record ParkingLotDetailResponse(
    Long id,
    String name,
    String categoryCode,
    String parkingTypeCode,
    String roadNameAddress,
    String lotNumberAddress,
    String phone,
    Integer totalSpaces,
    String operationDays,
    LocalTime weekdayOpen,
    LocalTime weekdayClose,
    LocalTime saturdayOpen,
    LocalTime saturdayClose,
    LocalTime holidayOpen,
    LocalTime holidayClose,
    String feeInfo,
    Integer baseMinutes,
    Integer baseFee,
    Integer extraUnitMinutes,
    Integer extraUnitFee,
    Integer dailyTicketHours,
    Integer dailyTicketFee,
    Integer monthlyPassFee,
    String paymentMethod,
    String specialNotes,
    String managementAgency,
    String disabledParkingYn,
    Double latitude,
    Double longitude
) {

    public static ParkingLotDetailResponse from(ParkingLot parkingLot) {
        return new ParkingLotDetailResponse(
            parkingLot.getId(),
            parkingLot.getName(),
            parkingLot.getCategoryCode(),
            parkingLot.getParkingTypeCode(),
            parkingLot.getRoadNameAddress(),
            parkingLot.getLotNumberAddress(),
            parkingLot.getPhone(),
            parkingLot.getTotalSpaces(),
            parkingLot.getOperationDays(),
            parkingLot.getWeekdayOpen(),
            parkingLot.getWeekdayClose(),
            parkingLot.getSaturdayOpen(),
            parkingLot.getSaturdayClose(),
            parkingLot.getHolidayOpen(),
            parkingLot.getHolidayClose(),
            parkingLot.getFeeInfo(),
            parkingLot.getBaseMinutes(),
            parkingLot.getBaseFee(),
            parkingLot.getExtraUnitMinutes(),
            parkingLot.getExtraUnitFee(),
            parkingLot.getDailyTicketHours(),
            parkingLot.getDailyTicketFee(),
            parkingLot.getMonthlyPassFee(),
            parkingLot.getPaymentMethod(),
            parkingLot.getSpecialNotes(),
            parkingLot.getManagementAgency(),
            parkingLot.getDisabledParkingYn(),
            parkingLot.getLocation().getLocation().getY(),
            parkingLot.getLocation().getLocation().getX()
        );
    }
}
