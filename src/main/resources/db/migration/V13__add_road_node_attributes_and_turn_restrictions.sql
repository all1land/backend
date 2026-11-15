-- ============================================================
-- ROAD NODE ATTRIBUTES
-- 표준노드링크 노드 파일(MOCT_NODE)의 속성. V9에서는 링크 끝점 좌표만으로 노드를 만들었다.
-- ============================================================

ALTER TABLE tb_road_node
    ADD COLUMN node_type_code       VARCHAR(3),
    ADD COLUMN node_name            VARCHAR(100),
    ADD COLUMN has_turn_restriction BOOLEAN NOT NULL DEFAULT FALSE;


-- ============================================================
-- ROAD TURN RESTRICTION
-- 표준노드링크 회전정보(TURNINFO). 진입·진출 링크가 모두 tb_road_link에 있는 행만 적재한다.
-- ============================================================

CREATE TABLE tb_road_turn_restriction (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    node_id             BIGINT NOT NULL,
    turn_seq            INTEGER NOT NULL,

    from_link_id        BIGINT NOT NULL,
    to_link_id          BIGINT NOT NULL,

    turn_type_code      VARCHAR(3) NOT NULL,
    operation_code      VARCHAR(1) NOT NULL,

    link_path           BIGINT[],

    CONSTRAINT fk_road_turn_restriction_node
        FOREIGN KEY (node_id)
        REFERENCES tb_road_node(id),

    CONSTRAINT fk_road_turn_restriction_from_link
        FOREIGN KEY (from_link_id)
        REFERENCES tb_road_link(id),

    CONSTRAINT fk_road_turn_restriction_to_link
        FOREIGN KEY (to_link_id)
        REFERENCES tb_road_link(id),

    CONSTRAINT uk_road_turn_restriction_node_turn_seq
        UNIQUE (node_id, turn_seq)
);


-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON COLUMN tb_road_node.node_type_code
    IS '노드유형 (NODE_TYPE). 101=교차로 시·종점, 102=도로 시·종점, 103=속성변환점, 104=도로시설물, 105=행정경계, 106=연결로접속부, 107=최소노드배치점, 109=기타';

COMMENT ON COLUMN tb_road_node.node_name
    IS '노드명칭 (NODE_NAME)';

COMMENT ON COLUMN tb_road_node.has_turn_restriction
    IS '회전제한유무 (TURN_P=1)';


COMMENT ON TABLE tb_road_turn_restriction
    IS '차량 네트워크 회전정보. 회전 제한(금지)과 허용된 특수 회전(U-TURN 등)이 함께 있다';

COMMENT ON COLUMN tb_road_turn_restriction.node_id
    IS '회전이 일어나는 노드 ID (NODE_ID). 진입 링크의 종료 노드와 같다';

COMMENT ON COLUMN tb_road_turn_restriction.turn_seq
    IS '노드 안 회전 일련번호 (TURN_ID)';

COMMENT ON COLUMN tb_road_turn_restriction.from_link_id
    IS '회전을 시작하는 진입 링크 ID (ST_LINK)';

COMMENT ON COLUMN tb_road_turn_restriction.to_link_id
    IS '회전이 끝나는 진출 링크 ID (ED_LINK)';

COMMENT ON COLUMN tb_road_turn_restriction.turn_type_code
    IS '회전제한유형 (TURN_TYPE). 001=비보호회전, 002=버스만회전, 003=회전금지, 011=U-TURN, 012=P-TURN, 101=좌회전금지, 102=직진금지, 103=우회전금지';

COMMENT ON COLUMN tb_road_turn_restriction.operation_code
    IS '회전제한운영 (TURN_OPER). 0=전일제, 1=시간제';

COMMENT ON COLUMN tb_road_turn_restriction.link_path
    IS '진입 링크부터 진출 링크까지 실제로 지나가는 링크 순서. 교차로는 접근 방향마다 노드가 따로 있어 좌회전·직진은 교차로 안 연결 링크를 거친다. 경로 탐색 대상 링크로 이어지지 않으면 NULL';
