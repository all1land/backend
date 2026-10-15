-- 전국주차장정보표준데이터 중 서울특별시 강서구 주차장 37건 적재
-- 팀 설계 기준: 좌표는 tb_location에 저장하고 tb_parking_lot.location_id로 참조
-- 원본: 전국주차장정보표준데이터.csv
-- 생성 기준일: 2026-10-13

-- 1. 신방화역
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.8177876, 37.5667867),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000010',
    '신방화역',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 마곡동 739',
    '02-3405-4581',
    199,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    290,
    5,
    290,
    0,
    0,
    109000,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 2. 개화역
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.798212, 37.5778861),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000002',
    '개화역',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 개화동 664',
    '02-2290-6312',
    483,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    290,
    5,
    290,
    0,
    0,
    109000,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 3. 가양3동
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.859775, 37.560338),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000004',
    '가양3동',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 가양동 1488-12',
    NULL,
    56,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    200,
    5,
    200,
    0,
    0,
    0,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 4. 가양역
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.853082, 37.563330),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000009',
    '가양역',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 가양동 1480-10',
    NULL,
    79,
    '평일+토요일+공휴일',
    TIME '08:00',
    TIME '21:00',
    TIME '08:00',
    TIME '21:00',
    TIME '08:00',
    TIME '21:00',
    '유료',
    5,
    320,
    5,
    320,
    0,
    0,
    0,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 5. 가양라이품
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.841653, 37.57170967),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000003',
    '가양라이품',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 가양동 1457-1',
    '02-2290-6144',
    38,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    200,
    5,
    200,
    0,
    0,
    109000,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 6. 개화산역
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.805190, 37.57213001),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000001',
    '개화산역',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 방화동 845',
    '02-2290-6064',
    321,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    290,
    5,
    290,
    0,
    0,
    109000,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 7. 방화역(동)
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.813315, 37.57729223),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000006',
    '방화역(동)',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 방화동 830-4',
    NULL,
    66,
    '평일+토요일+공휴일',
    TIME '08:00',
    TIME '21:00',
    TIME '08:00',
    TIME '21:00',
    TIME '08:00',
    TIME '21:00',
    '유료',
    5,
    290,
    5,
    290,
    0,
    0,
    0,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 8. 방화역(서)
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.812175, 37.577574),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000007',
    '방화역(서)',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 방화동 829-1',
    NULL,
    69,
    '평일+토요일+공휴일',
    TIME '08:00',
    TIME '21:00',
    TIME '08:00',
    TIME '21:00',
    TIME '08:00',
    TIME '21:00',
    '유료',
    5,
    290,
    5,
    290,
    0,
    0,
    0,
    '카드',
    NULL,
    '서울시설공단',
    'Y',
    DATE '2026-05-15',
    'B553774',
    '서울시설공단'
FROM new_location;

-- 9. 화곡8-2
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.850354, 37.537679),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000017',
    '화곡8-2',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 초록마을로 126',
    '서울특별시 강서구 화곡동 410-4',
    '02-2065-1575',
    109,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 10. 등서초
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.855038, 37.551884),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-3-000003',
    '등서초',
    'PUBLIC',
    'ATTACHED',
    '서울특별시 강서구 화곡로58길 86',
    '서울특별시 강서구 화곡동 1130-4',
    '02-2065-1575',
    32,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 11. 화곡1-2
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.8410246, 37.5338224),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000021',
    '화곡1-2',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 화곡동 366-47',
    '02-2065-1575',
    9,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'N',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 12. 화곡4-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.861044, 37.534301),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-3-000001',
    '화곡4-1',
    'PUBLIC',
    'ATTACHED',
    '서울특별시 강서구 곰달래로57가길 20',
    '서울특별시 강서구 화곡동 809-26',
    '02-2065-1575',
    202,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 13. 화곡6-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.848514, 37.553032),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000010',
    '화곡6-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 우장산로 114',
    '서울특별시 강서구 화곡동 1103-6',
    '02-2065-1575',
    298,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    100,
    5,
    100,
    NULL,
    NULL,
    70000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 14. 볏골공원
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.844323, 37.541816),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000011',
    '볏골공원',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 까치산로4길 22',
    '서울특별시 강서구 화곡동 98-99',
    '02-2065-1575',
    228,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    100,
    5,
    100,
    NULL,
    NULL,
    60000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 15. 곰달래문화복지센터
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.848898, 37.529060),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-3-000002',
    '곰달래문화복지센터',
    'PUBLIC',
    'ATTACHED',
    '서울특별시 강서구 강서로5길 50',
    '서울특별시 강서구 화곡동 890',
    '02-2065-1575',
    252,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    100,
    5,
    100,
    NULL,
    NULL,
    80000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 16. 가로공원로지하
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.837039, 37.537235),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000013',
    '가로공원로지하',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 화곡동 662-15',
    '02-2065-1575',
    512,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    100,
    5,
    100,
    NULL,
    NULL,
    60000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 17. 서울식물원 제2주차장
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.835236, 37.572662),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000018',
    '서울식물원 제2주차장',
    'PUBLIC',
    'OFF_STREET',
    NULL,
    '서울특별시 강서구 마곡동 20-3',
    '02-2065-1575',
    202,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    100,
    5,
    100,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 18. 화곡본-2
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.848448, 37.5443203),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000001',
    '화곡본-2',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 초록마을로5길 43',
    '서울특별시 강서구 화곡동 46-127',
    '02-2065-1575',
    100,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 19. 화곡2-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.852432, 37.536046),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000002',
    '화곡2-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 강서로18아길 26-15',
    '서울특별시 강서구 화곡동 167-10',
    '02-2065-1575',
    58,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 20. 화곡3-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.834727, 37.541769),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000003',
    '화곡3-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 화곡로15길 40',
    '서울특별시 강서구 화곡동 1045',
    '02-2065-1575',
    99,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 21. 화곡5-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.839922, 37.5438705),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000005',
    '화곡5-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 강서로36길 21',
    '서울특별시 강서구 화곡동 1030-33',
    '02-2065-1575',
    26,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 22. 화곡5-2
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.840763, 37.544352),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000006',
    '화곡5-2',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 강서로34길 41-21',
    '서울특별시 강서구 화곡동 1056-2',
    '02-2065-1575',
    27,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 23. 화곡5-3
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.842258, 37.543454),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000007',
    '화곡5-3',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 화곡로27가길 38',
    '서울특별시 강서구 화곡동 1060-18',
    '02-2065-1575',
    13,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 24. 화곡7-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.839155, 37.536016),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000008',
    '화곡7-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 월정로32길 68',
    '서울특별시 강서구 화곡동 370-63',
    '02-2065-1575',
    86,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 25. 등촌2-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.861316, 37.549799),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000009',
    '등촌2-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 등촌로55길 33-16',
    '서울특별시 강서구 등촌동 531-5',
    '02-2065-1575',
    110,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 26. 화곡본-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.849772, 37.539744),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000014',
    '화곡본-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 초록마을로26길 48',
    '서울특별시 강서구 화곡동56-127',
    '02-2065-1575',
    98,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 27. 화곡1-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.839874, 37.5314204),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000015',
    '화곡1-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 곰달래로15길 28',
    '서울특별시 강서구 화곡동424-94',
    '02-2065-1575',
    99,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 28. 화곡8-1
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.848840, 37.538511),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-2-000016',
    '화곡8-1',
    'PUBLIC',
    'OFF_STREET',
    '서울특별시 강서구 초록마을로22길 57-11',
    '서울특별시 강서구 화곡동 410-60',
    '02-2065-1575',
    59,
    '평일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 29. 봉제산근린공원
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.8510719, 37.5421344),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-3-000004',
    '봉제산근린공원',
    'PUBLIC',
    'ATTACHED',
    NULL,
    '서울특별시 강서구 화곡동 산41-6',
    '02-2065-1575',
    56,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    50,
    5,
    50,
    NULL,
    NULL,
    50000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 30. 마곡안전체험관
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.8258394, 37.5545411),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-3-000005',
    '마곡안전체험관',
    'PUBLIC',
    'ATTACHED',
    '서울특별시 강서구 마곡중앙로 13',
    '서울특별시 강서구 내발산동 739',
    '02-2065-1575',
    36,
    '평일+토요일+공휴일',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    TIME '00:00',
    TIME '23:59',
    '유료',
    5,
    100,
    5,
    100,
    NULL,
    NULL,
    70000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 31. 화곡1동 달구지길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.837414, 37.532988),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000020',
    '화곡1동 달구지길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 월정로28길 14',
    '서울특별시 강서구 화곡동 359-2',
    '02-2607-9113',
    29,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    250,
    5,
    250,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 32. 화곡1동 배다리서길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.847886, 37.528779),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000019',
    '화곡1동 배다리서길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 강서로5나길 6',
    '서울특별시 강서구 화곡동 904-2',
    '02-2607-9113',
    249,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    200,
    5,
    200,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 33. 화곡2,4,8동 배다리동길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.849947, 37.529024),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000021',
    '화곡2,4,8동 배다리동길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 국회대로7길 196',
    '서울특별시 강서구 화곡동 891-13',
    '02-2607-9113',
    304,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    200,
    5,
    200,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 34. 화곡6동 방죽길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.8524896, 37.554165),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000022',
    '화곡6동 방죽길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 공항대로46길 35',
    '서울특별시 강서구 화곡동 1112-1',
    '02-2607-9113',
    47,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    250,
    5,
    250,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 35. 화곡6동 신학대길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.849532, 37.550966),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000023',
    '화곡6동 신학대길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 화곡로 302',
    '서울특별시 강서구 화곡동 980-16',
    '02-2607-9113',
    29,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    250,
    5,
    250,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 36. 등촌3동 구암길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.844636, 37.561217),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000024',
    '등촌3동 구암길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 공항대로39길 74',
    '서울특별시 강서구 등촌동 695',
    '02-2607-9113',
    49,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    250,
    5,
    250,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;

-- 37. 등촌3동 꿈나무길
WITH new_location AS (
    INSERT INTO tb_location (location)
    VALUES (
        ST_SetSRID(
            ST_MakePoint(126.847619, 37.560599),
            4326
        )::geography
    )
    RETURNING id
)
INSERT INTO tb_parking_lot (
    location_id,
    external_id,
    name,
    category_code,
    parking_type_code,
    road_name_address,
    lot_number_address,
    phone,
    total_spaces,
    operation_days,
    weekday_open,
    weekday_close,
    saturday_open,
    saturday_close,
    holiday_open,
    holiday_close,
    fee_info,
    base_minutes,
    base_fee,
    extra_unit_minutes,
    extra_unit_fee,
    daily_ticket_hours,
    daily_ticket_fee,
    monthly_pass_fee,
    payment_method,
    special_notes,
    management_agency,
    disabled_parking_yn,
    data_reference_date,
    provider_code,
    provider_name
)
SELECT
    id,
    '115-1-000025',
    '등촌3동 꿈나무길',
    'PUBLIC',
    'ON_STREET',
    '서울특별시 강서구 공항대로 351-21',
    '서울특별시 강서구 등촌동 701',
    '02-2607-9113',
    60,
    '평일+토요일',
    TIME '09:00',
    TIME '19:00',
    TIME '09:00',
    TIME '15:00',
    TIME '00:00',
    TIME '00:00',
    '유료',
    5,
    250,
    5,
    250,
    NULL,
    NULL,
    120000,
    '신용카드',
    '국가유공상이자,장애인,고엽제후유의증환자 80할인+경차 및 저공해자동차 50할인',
    '서울특별시 강서구청',
    'Y',
    DATE '2026-04-17',
    '3150000',
    '서울특별시 강서구'
FROM new_location;
