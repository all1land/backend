CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;


-- ============================================================
-- COMMON CODE
-- ============================================================

CREATE TABLE tb_cmn_code (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id   BIGINT,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    level       INTEGER      NOT NULL DEFAULT 1,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    description VARCHAR(500),
    state       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,

    CONSTRAINT fk_cmn_code_parent
        FOREIGN KEY (parent_id)
        REFERENCES tb_cmn_code(id),

    CONSTRAINT uk_cmn_code_parent_code
        UNIQUE (parent_id, code),

    CONSTRAINT ck_cmn_code_state
        CHECK (state IN ('ACTIVE', 'INACTIVE'))
);


-- ============================================================
-- USER
-- ============================================================

CREATE TABLE tb_user (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'CLIENT',
    state       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    img_url     VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    deleted_at  TIMESTAMP,

    CONSTRAINT uk_user_username
        UNIQUE (username),

    CONSTRAINT uk_user_email
        UNIQUE (email),

    CONSTRAINT ck_user_role
        CHECK (role IN ('CLIENT', 'ADMIN', 'SUPER_ADMIN')),

    CONSTRAINT ck_user_state
        CHECK (state IN ('INACTIVE', 'ACTIVE', 'SUSPENDED'))
);


-- ============================================================
-- LOCATION
-- 공간정보 공통 테이블
-- ============================================================

CREATE TABLE tb_location (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    location            geography(Point, 4326) NOT NULL,
    entrance_location   geography(Point, 4326),

    created_at          TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

CREATE INDEX idx_location_location
    ON tb_location USING GIST (location);

CREATE INDEX idx_location_entrance_location
    ON tb_location USING GIST (entrance_location);


-- ============================================================
-- PLACE
-- 일반 목적지 / POI
-- ============================================================

CREATE TABLE tb_place (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    location_id         BIGINT NOT NULL,

    name                VARCHAR(255) NOT NULL,

    -- tb_cmn_code 의 PLACE_CATEGORY 하위 코드
    category_code       VARCHAR(50),

    road_name_address   VARCHAR(255) NOT NULL,
    lot_number_address  VARCHAR(255),
    zip_code            VARCHAR(10),
    phone               VARCHAR(30),

    created_at          TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT fk_place_location
        FOREIGN KEY (location_id)
        REFERENCES tb_location(id),

    CONSTRAINT uk_place_road_name_address_name
        UNIQUE (road_name_address, name)
);

CREATE INDEX idx_place_name_trgm
    ON tb_place USING GIN (name gin_trgm_ops);

CREATE INDEX idx_place_road_name_address_trgm
    ON tb_place USING GIN (road_name_address gin_trgm_ops);


-- ============================================================
-- PARKING LOT
-- ============================================================

CREATE TABLE tb_parking_lot (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    location_id         BIGINT NOT NULL,

    external_id         VARCHAR(50)  NOT NULL,
    name                VARCHAR(255) NOT NULL,

    -- tb_cmn_code 의 PARKING_CATEGORY 하위 코드
    category_code       VARCHAR(50),

    road_name_address   VARCHAR(255),
    lot_number_address  VARCHAR(255),
    phone               VARCHAR(30),

    total_spaces        INTEGER,

    weekday_open        TIME,
    weekday_close       TIME,

    saturday_open       TIME,
    saturday_close      TIME,

    holiday_open        TIME,
    holiday_close       TIME,

    base_minutes        INTEGER,
    base_fee            INTEGER,

    extra_unit_minutes  INTEGER,
    extra_unit_fee      INTEGER,

    daily_max_fee       INTEGER,
    monthly_pass_fee    INTEGER,

    created_at          TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT fk_parking_lot_location
        FOREIGN KEY (location_id)
        REFERENCES tb_location(id),

    CONSTRAINT uk_parking_lot_external_id
        UNIQUE (external_id),

    CONSTRAINT ck_parking_lot_total_spaces
        CHECK (total_spaces IS NULL OR total_spaces >= 0),

    CONSTRAINT ck_parking_lot_base_minutes
        CHECK (base_minutes IS NULL OR base_minutes >= 0),

    CONSTRAINT ck_parking_lot_base_fee
        CHECK (base_fee IS NULL OR base_fee >= 0),

    CONSTRAINT ck_parking_lot_extra_unit_minutes
        CHECK (extra_unit_minutes IS NULL OR extra_unit_minutes >= 0),

    CONSTRAINT ck_parking_lot_extra_unit_fee
        CHECK (extra_unit_fee IS NULL OR extra_unit_fee >= 0),

    CONSTRAINT ck_parking_lot_daily_max_fee
        CHECK (daily_max_fee IS NULL OR daily_max_fee >= 0),

    CONSTRAINT ck_parking_lot_monthly_pass_fee
        CHECK (monthly_pass_fee IS NULL OR monthly_pass_fee >= 0)
);

CREATE INDEX idx_parking_lot_name_trgm
    ON tb_parking_lot USING GIN (name gin_trgm_ops);

CREATE INDEX idx_parking_lot_road_name_address_trgm
    ON tb_parking_lot USING GIN (road_name_address gin_trgm_ops);


-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON TABLE tb_cmn_code
    IS '서비스 공통코드';

COMMENT ON COLUMN tb_cmn_code.parent_id
    IS '상위 공통코드 ID';

COMMENT ON COLUMN tb_cmn_code.code
    IS '공통코드 값';

COMMENT ON COLUMN tb_cmn_code.level
    IS '공통코드 계층 깊이';


COMMENT ON TABLE tb_location
    IS '장소 및 주차장에서 공통으로 사용하는 공간정보';

COMMENT ON COLUMN tb_location.location
    IS '대표 위치 좌표. 지도 마커 및 반경 검색에 사용';

COMMENT ON COLUMN tb_location.entrance_location
    IS '실제 경로 안내에 사용할 출입구 좌표';


COMMENT ON TABLE tb_place
    IS '일반 목적지 및 POI 정보';

COMMENT ON COLUMN tb_place.location_id
    IS 'tb_location 참조 ID';

COMMENT ON COLUMN tb_place.name
    IS '목적지 또는 POI 명';

COMMENT ON COLUMN tb_place.category_code
    IS '공통코드 PLACE_CATEGORY 하위 코드';

COMMENT ON COLUMN tb_place.road_name_address
    IS '도로명 주소';


COMMENT ON TABLE tb_parking_lot
    IS '주차장 정보';

COMMENT ON COLUMN tb_parking_lot.location_id
    IS 'tb_location 참조 ID';

COMMENT ON COLUMN tb_parking_lot.external_id
    IS '공공데이터 주차장관리번호. upsert 및 외부 데이터 연동 키';

COMMENT ON COLUMN tb_parking_lot.category_code
    IS '공통코드 PARKING_CATEGORY 하위 코드';

COMMENT ON COLUMN tb_parking_lot.total_spaces
    IS '전체 주차면 수';

COMMENT ON COLUMN tb_parking_lot.base_minutes
    IS '기본 요금 적용 시간(분)';

COMMENT ON COLUMN tb_parking_lot.base_fee
    IS '기본 요금';

COMMENT ON COLUMN tb_parking_lot.extra_unit_minutes
    IS '추가 요금 단위 시간(분)';

COMMENT ON COLUMN tb_parking_lot.extra_unit_fee
    IS '추가 단위 요금';

COMMENT ON COLUMN tb_parking_lot.daily_max_fee
    IS '일 최대 요금';

COMMENT ON COLUMN tb_parking_lot.monthly_pass_fee
    IS '월 정기권 요금';