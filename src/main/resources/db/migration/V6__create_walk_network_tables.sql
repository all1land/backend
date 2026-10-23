CREATE EXTENSION IF NOT EXISTS pgrouting;


-- ============================================================
-- WALK NETWORK
-- 서울시 자치구별 도보 네트워크 공간정보 (서울 열린데이터광장 OA-21208)
-- id는 원본 노드/링크 ID를 그대로 사용한다.
-- ============================================================

CREATE TABLE tb_walk_node (
    id                  BIGINT PRIMARY KEY,

    node_type_code      VARCHAR(10),

    geom                geometry(Point, 4326) NOT NULL
);

CREATE INDEX idx_walk_node_geom
    ON tb_walk_node USING GIST (geom);


CREATE TABLE tb_walk_link (
    id                  BIGINT PRIMARY KEY,

    start_node_id       BIGINT NOT NULL,
    end_node_id         BIGINT NOT NULL,

    link_type_code      VARCHAR(4) NOT NULL,
    length_m            DOUBLE PRECISION NOT NULL,

    is_elevated         BOOLEAN NOT NULL DEFAULT FALSE,
    is_subway_network   BOOLEAN NOT NULL DEFAULT FALSE,
    is_bridge           BOOLEAN NOT NULL DEFAULT FALSE,
    is_tunnel           BOOLEAN NOT NULL DEFAULT FALSE,
    is_overpass         BOOLEAN NOT NULL DEFAULT FALSE,
    is_crosswalk        BOOLEAN NOT NULL DEFAULT FALSE,
    is_park             BOOLEAN NOT NULL DEFAULT FALSE,
    is_in_building      BOOLEAN NOT NULL DEFAULT FALSE,

    geom                geometry(LineString, 4326) NOT NULL,

    CONSTRAINT fk_walk_link_start_node
        FOREIGN KEY (start_node_id)
        REFERENCES tb_walk_node(id),

    CONSTRAINT fk_walk_link_end_node
        FOREIGN KEY (end_node_id)
        REFERENCES tb_walk_node(id),

    CONSTRAINT ck_walk_link_length_m
        CHECK (length_m >= 0)
);

CREATE INDEX idx_walk_link_geom
    ON tb_walk_link USING GIST (geom);


-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON TABLE tb_walk_node
    IS '도보 네트워크 노드';

COMMENT ON COLUMN tb_walk_node.id
    IS '원본 노드 ID. 구 경계 밖 노드는 원본에 없어 링크 끝점 좌표로 생성';

COMMENT ON COLUMN tb_walk_node.node_type_code
    IS '원본 노드 유형 코드. 0=일반, 1=지하철 출입구, 2=버스 정류장, 3=지하보도 출입구. 링크 끝점으로 생성한 노드는 NULL';

COMMENT ON COLUMN tb_walk_node.geom
    IS '노드 좌표 (WGS84)';


COMMENT ON TABLE tb_walk_link
    IS '도보 네트워크 링크. pgRouting 경로 탐색 대상';

COMMENT ON COLUMN tb_walk_link.id
    IS '원본 링크 ID';

COMMENT ON COLUMN tb_walk_link.start_node_id
    IS '시작 노드 ID. geom의 첫 점과 일치. pgRouting 쿼리에서 source로 사용';

COMMENT ON COLUMN tb_walk_link.end_node_id
    IS '종료 노드 ID. geom의 끝 점과 일치. pgRouting 쿼리에서 target으로 사용';

COMMENT ON COLUMN tb_walk_link.link_type_code
    IS '원본 링크 유형 코드. 4자리 비트 플래그(보행자/차량/자전거/PM 순, 1=통행 가능)';

COMMENT ON COLUMN tb_walk_link.length_m
    IS '링크 길이(m). 경로 탐색 cost로 사용';

COMMENT ON COLUMN tb_walk_link.is_elevated
    IS '고가도로 여부';

COMMENT ON COLUMN tb_walk_link.is_subway_network
    IS '지하철 네트워크 여부';

COMMENT ON COLUMN tb_walk_link.is_in_building
    IS '건물 내 통로 여부';

COMMENT ON COLUMN tb_walk_link.geom
    IS '링크 선형 (WGS84). 꼭짓점이 여러 개일 수 있음';
