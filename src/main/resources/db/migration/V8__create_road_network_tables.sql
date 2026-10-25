-- ============================================================
-- ROAD NETWORK
-- 국토교통부 ITS 표준노드링크(구간단위) 차량 네트워크
-- id는 원본 노드/링크 ID를 그대로 사용한다.
-- ============================================================

CREATE TABLE tb_road_node (
    id                  BIGINT PRIMARY KEY,

    geom                geometry(Point, 4326) NOT NULL
);

CREATE INDEX idx_road_node_geom
    ON tb_road_node USING GIST (geom);


CREATE TABLE tb_road_link (
    id                  BIGINT PRIMARY KEY,

    start_node_id       BIGINT NOT NULL,
    end_node_id         BIGINT NOT NULL,

    lanes               INTEGER,
    road_rank_code      VARCHAR(3) NOT NULL,
    road_type_code      VARCHAR(3) NOT NULL,
    road_name           VARCHAR(100),

    is_in_use           BOOLEAN NOT NULL,
    is_ramp             BOOLEAN NOT NULL,
    restricted_vehicle_code VARCHAR(3) NOT NULL,

    max_speed_kph       INTEGER NOT NULL,
    length_m            DOUBLE PRECISION NOT NULL,
    travel_time_s       DOUBLE PRECISION GENERATED ALWAYS AS (length_m / (max_speed_kph / 3.6)) STORED,

    is_routable         BOOLEAN NOT NULL DEFAULT FALSE,

    geom                geometry(LineString, 4326) NOT NULL,

    CONSTRAINT fk_road_link_start_node
        FOREIGN KEY (start_node_id)
        REFERENCES tb_road_node(id),

    CONSTRAINT fk_road_link_end_node
        FOREIGN KEY (end_node_id)
        REFERENCES tb_road_node(id),

    CONSTRAINT ck_road_link_max_speed_kph
        CHECK (max_speed_kph > 0),

    CONSTRAINT ck_road_link_length_m
        CHECK (length_m >= 0)
);

CREATE INDEX idx_road_link_geom
    ON tb_road_link USING GIST (geom);


-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON TABLE tb_road_node
    IS '차량 네트워크 노드. 원본 노드 파일 대신 링크 끝점에서 생성';

COMMENT ON COLUMN tb_road_node.id
    IS '원본 노드 ID (NODE_ID)';

COMMENT ON COLUMN tb_road_node.geom
    IS '노드 좌표 (WGS84)';


COMMENT ON TABLE tb_road_link
    IS '차량 네트워크 링크. 방향별로 따로 존재하며 pgRouting 경로 탐색 대상';

COMMENT ON COLUMN tb_road_link.id
    IS '원본 링크 ID (LINK_ID)';

COMMENT ON COLUMN tb_road_link.start_node_id
    IS '시작 노드 ID (F_NODE). 차량 진행 방향의 시작. geom의 첫 점과 일치';

COMMENT ON COLUMN tb_road_link.end_node_id
    IS '종료 노드 ID (T_NODE). 차량 진행 방향의 끝. geom의 끝 점과 일치';

COMMENT ON COLUMN tb_road_link.lanes
    IS '차로 수 (LANES)';

COMMENT ON COLUMN tb_road_link.road_rank_code
    IS '도로등급 (ROAD_RANK). 101=고속국도, 102=도시고속국도, 103=일반국도, 104=특별·광역시도, 105=국가지원지방도, 106=지방도, 107=시군도, 108=기타';

COMMENT ON COLUMN tb_road_link.road_type_code
    IS '도로유형 (ROAD_TYPE). 000=일반도로, 001=고가차도, 002=지하차도, 003=교량, 004=터널';

COMMENT ON COLUMN tb_road_link.road_name
    IS '도로명 (ROAD_NAME)';

COMMENT ON COLUMN tb_road_link.is_in_use
    IS '차량 통행 가능 여부 (ROAD_USE=0). 일방통행 도로의 역방향 링크는 미사용(ROAD_USE=1)으로 존재';

COMMENT ON COLUMN tb_road_link.is_ramp
    IS '연결로(램프) 여부 (CONNECT=1)';

COMMENT ON COLUMN tb_road_link.restricted_vehicle_code
    IS '통행제한차량 (REST_VEH). 0=모두 통행 가능, 1=승용차, 2=승합차, 3=버스, 4=트럭, 5=이륜차, 6=기타';

COMMENT ON COLUMN tb_road_link.max_speed_kph
    IS '최고 제한속도 (MAX_SPD, km/h)';

COMMENT ON COLUMN tb_road_link.length_m
    IS '링크 길이 (LENGTH, m)';

COMMENT ON COLUMN tb_road_link.travel_time_s
    IS '제한속도로 주행할 때 걸리는 시간(초). 경로 탐색 cost로 사용';

COMMENT ON COLUMN tb_road_link.is_routable
    IS '승용차 경로 탐색 대상 여부. 통행 가능하고 가장 큰 강연결 요소에 속한 링크. 적재 시 계산';

COMMENT ON COLUMN tb_road_link.geom
    IS '링크 선형 (WGS84). 시작 노드 → 종료 노드 방향';
