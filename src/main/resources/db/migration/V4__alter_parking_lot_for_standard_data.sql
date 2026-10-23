-- 전국주차장정보표준데이터 적재를 위한 tb_parking_lot 보완

-- 원본 데이터의 주차장유형(노외/노상/부설)
ALTER TABLE tb_parking_lot
    ADD COLUMN parking_type_code VARCHAR(50);

-- 운영/요금/관리기관 등 서비스에 필요한 원본 속성
ALTER TABLE tb_parking_lot
    ADD COLUMN operation_days VARCHAR(50),
    ADD COLUMN fee_info VARCHAR(20),
    ADD COLUMN payment_method VARCHAR(100),
    ADD COLUMN special_notes TEXT,
    ADD COLUMN management_agency VARCHAR(255),
    ADD COLUMN disabled_parking_yn CHAR(1),
    ADD COLUMN data_reference_date DATE,
    ADD COLUMN provider_code VARCHAR(30),
    ADD COLUMN provider_name VARCHAR(255),
    ADD COLUMN daily_ticket_hours INTEGER;

-- 표준데이터의 '1일주차권요금'은 '일 최대 요금'과 의미가 다르므로 명확히 변경
ALTER TABLE tb_parking_lot
    DROP CONSTRAINT IF EXISTS ck_parking_lot_daily_max_fee;

ALTER TABLE tb_parking_lot
    RENAME COLUMN daily_max_fee TO daily_ticket_fee;

ALTER TABLE tb_parking_lot
    ADD CONSTRAINT ck_parking_lot_daily_ticket_fee
    CHECK (daily_ticket_fee IS NULL OR daily_ticket_fee >= 0);

ALTER TABLE tb_parking_lot
    ADD CONSTRAINT ck_parking_lot_daily_ticket_hours
    CHECK (daily_ticket_hours IS NULL OR daily_ticket_hours >= 0);

ALTER TABLE tb_parking_lot
    ADD CONSTRAINT ck_parking_lot_disabled_parking_yn
    CHECK (disabled_parking_yn IS NULL OR disabled_parking_yn IN ('Y', 'N'));

-- 실제 표준데이터에는 서로 다른 제공기관이 같은 주차장관리번호를 사용하는 사례가 있음.
-- 따라서 external_id 단독 UNIQUE 대신 제공기관코드 + 주차장관리번호 조합으로 식별.
ALTER TABLE tb_parking_lot
    DROP CONSTRAINT IF EXISTS uk_parking_lot_external_id;

ALTER TABLE tb_parking_lot
    ALTER COLUMN provider_code SET NOT NULL;

ALTER TABLE tb_parking_lot
    ADD CONSTRAINT uk_parking_lot_provider_external_id
    UNIQUE (provider_code, external_id);

COMMENT ON COLUMN tb_parking_lot.parking_type_code
    IS '공통코드 PARKING_TYPE 하위 코드. 노외/노상/부설';

COMMENT ON COLUMN tb_parking_lot.operation_days
    IS '전국주차장정보표준데이터 운영요일';

COMMENT ON COLUMN tb_parking_lot.fee_info
    IS '전국주차장정보표준데이터 요금정보';

COMMENT ON COLUMN tb_parking_lot.daily_ticket_hours
    IS '1일주차권요금 적용시간';

COMMENT ON COLUMN tb_parking_lot.daily_ticket_fee
    IS '1일주차권요금';

COMMENT ON COLUMN tb_parking_lot.provider_code
    IS '공공데이터 제공기관코드';

COMMENT ON COLUMN tb_parking_lot.provider_name
    IS '공공데이터 제공기관명';
