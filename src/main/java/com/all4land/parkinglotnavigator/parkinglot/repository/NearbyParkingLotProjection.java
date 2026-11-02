package com.all4land.parkinglotnavigator.parkinglot.repository;

public interface NearbyParkingLotProjection {
    Long getId();

    String getName();

    String getRoadNameAddress();

    String getPhone();

    String getLotNumberAddress();

    String getCategoryCode();

    String getParkingTypeCode();

    Integer getTotalSpaces();

    Double getLatitude();

    Double getLongitude();

    Double getDistance();
}
