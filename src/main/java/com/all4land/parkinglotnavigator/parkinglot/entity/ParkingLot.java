package com.all4land.parkinglotnavigator.parkinglot.entity;

import com.all4land.parkinglotnavigator.global.entity.BaseEntity;
import com.all4land.parkinglotnavigator.location.entity.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "tb_parking_lot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingLot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "external_id", nullable = false, length = 50)
    private String externalId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "parking_type_code", length = 50)
    private String parkingTypeCode;

    @Column(name = "road_name_address", length = 255)
    private String roadNameAddress;

    @Column(name = "lot_number_address", length = 255)
    private String lotNumberAddress;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "total_spaces")
    private Integer totalSpaces;

    @Column(name = "operation_days", length = 50)
    private String operationDays;

    @Column(name = "weekday_open")
    private LocalTime weekdayOpen;

    @Column(name = "weekday_close")
    private LocalTime weekdayClose;

    @Column(name = "saturday_open")
    private LocalTime saturdayOpen;

    @Column(name = "saturday_close")
    private LocalTime saturdayClose;

    @Column(name = "holiday_open")
    private LocalTime holidayOpen;

    @Column(name = "holiday_close")
    private LocalTime holidayClose;

    @Column(name = "fee_info", length = 20)
    private String feeInfo;

    @Column(name = "base_minutes")
    private Integer baseMinutes;

    @Column(name = "base_fee")
    private Integer baseFee;

    @Column(name = "extra_unit_minutes")
    private Integer extraUnitMinutes;

    @Column(name = "extra_unit_fee")
    private Integer extraUnitFee;

    @Column(name = "daily_ticket_hours")
    private Integer dailyTicketHours;

    @Column(name = "daily_ticket_fee")
    private Integer dailyTicketFee;

    @Column(name = "monthly_pass_fee")
    private Integer monthlyPassFee;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "management_agency", length = 255)
    private String managementAgency;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "disabled_parking_yn", length = 1)
    private String disabledParkingYn;

    @Column(name = "data_reference_date")
    private LocalDate dataReferenceDate;

    @Column(name = "provider_code", nullable = false, length = 30)
    private String providerCode;

    @Column(name = "provider_name", length = 255)
    private String providerName;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}
