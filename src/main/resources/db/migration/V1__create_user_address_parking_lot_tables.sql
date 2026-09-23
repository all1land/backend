CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

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

    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT ck_user_role CHECK (role IN ('CLIENT', 'ADMIN', 'SUPER_ADMIN')),
    CONSTRAINT ck_user_state CHECK (state IN ('INACTIVE', 'ACTIVE', 'SUSPENDED'))
);

CREATE TABLE tb_address (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(255)           NOT NULL,
    category            VARCHAR(100),
    road_name_address   VARCHAR(255)           NOT NULL,
    lot_number_address  VARCHAR(255),
    zip_code            VARCHAR(10),
    phone               VARCHAR(30),
    location            geography(Point, 4326) NOT NULL,
    entrance_location   geography(Point, 4326),
    created_at          TIMESTAMP              NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at          TIMESTAMP              NOT NULL DEFAULT LOCALTIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT uk_address_road_name_address_name UNIQUE (road_name_address, name)
);

CREATE INDEX idx_address_location ON tb_address USING GIST (location);
CREATE INDEX idx_address_name_trgm ON tb_address USING GIN (name gin_trgm_ops);
CREATE INDEX idx_address_road_name_address_trgm ON tb_address USING GIN (road_name_address gin_trgm_ops);

CREATE TABLE tb_parking_lot (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_id         VARCHAR(50)            NOT NULL,
    name                VARCHAR(255)           NOT NULL,
    category            VARCHAR(20),
    road_name_address   VARCHAR(255),
    lot_number_address  VARCHAR(255),
    phone               VARCHAR(30),
    location            geography(Point, 4326) NOT NULL,
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
    created_at          TIMESTAMP              NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at          TIMESTAMP              NOT NULL DEFAULT LOCALTIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT uk_parking_lot_external_id UNIQUE (external_id)
);

CREATE INDEX idx_parking_lot_location ON tb_parking_lot USING GIST (location);

COMMENT ON COLUMN tb_address.name IS 'POI명 (도로명 주소와 함께 upsert 키)';
COMMENT ON COLUMN tb_address.category IS '업종';
COMMENT ON COLUMN tb_address.location IS 'POI 중심 좌표 (마커, 반경 검색)';
COMMENT ON COLUMN tb_address.entrance_location IS '입구 좌표 (경로 안내 목적지)';
COMMENT ON COLUMN tb_parking_lot.external_id IS '공공데이터 주차장관리번호 (upsert 키, 실시간 주차 정보 연동 키)';
COMMENT ON COLUMN tb_parking_lot.category IS '공영/민영';
COMMENT ON COLUMN tb_parking_lot.total_spaces IS '전체 주차면 수';
COMMENT ON COLUMN tb_parking_lot.base_minutes IS '기본 요금 적용 시간(분)';
COMMENT ON COLUMN tb_parking_lot.extra_unit_minutes IS '추가 요금 단위 시간(분)';
