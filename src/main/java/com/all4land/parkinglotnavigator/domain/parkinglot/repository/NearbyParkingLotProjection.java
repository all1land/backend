package com.all4land.parkinglotnavigator.domain.parkinglot.repository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface NearbyParkingLotProjection {

    Long getId();

    String getName();

    String getRoadNameAddress();

    String getPhone();

    String getLotNumberAddress();

    String getCategoryCode();

    String getCategoryName();

    String getParkingTypeCode();

    String getParkingTypeName();

    Integer getTotalSpaces();

    Double getLatitude();

    Double getLongitude();

    Double getDistance();

    String getExternalId();

    String getDisplayAddress();

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

    LocalDate getDataReferenceDate();

    String getProviderCode();

    String getProviderName();

    Double getEntranceLatitude();

    Double getEntranceLongitude();
}
