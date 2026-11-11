package com.all4land.parkinglotnavigator.domain.parkinglot.repository;

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
}
