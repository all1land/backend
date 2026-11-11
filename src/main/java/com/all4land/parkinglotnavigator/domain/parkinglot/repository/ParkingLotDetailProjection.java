package com.all4land.parkinglotnavigator.domain.parkinglot.repository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ParkingLotDetailProjection {

    Long getId();

    String getName();

    String getCategoryCode();

    String getCategoryName();

    String getParkingTypeCode();

    String getParkingTypeName();

    String getRoadNameAddress();

    String getLotNumberAddress();

    String getPhone();

    Integer getTotalSpaces();

    String getOperationDays();

    LocalTime getWeekdayOpen();

    LocalTime getWeekdayClose();

    LocalTime getSaturdayOpen();

    LocalTime getSaturdayClose();

    LocalTime getHolidayOpen();

    LocalTime getHolidayClose();

    String getFeeInfo();

    Integer getBaseMinutes();

    Integer getBaseFee();

    Integer getExtraUnitMinutes();

    Integer getExtraUnitFee();

    Integer getDailyTicketHours();

    Integer getDailyTicketFee();

    Integer getMonthlyPassFee();

    String getPaymentMethod();

    String getSpecialNotes();

    String getManagementAgency();

    String getDisabledParkingYn();

    Double getLatitude();

    Double getLongitude();

    String getExternalId();

    String getDisplayAddress();

    Double getEntranceLatitude();

    Double getEntranceLongitude();

    String getProviderCode();

    String getProviderName();

    LocalDate getDataReferenceDate();
}
