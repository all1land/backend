-- ============================================================
-- ROAD U-TURN PROHIBITION
-- 교차로는 접근 방향마다 노드가 따로 있어서, 교차로 안 연결 링크를 돌아 같은 도로 반대 방향으로 빠져나가는 U턴이 가능하다.
-- 실제로는 U턴 허용 지점(회전정보 011 U-TURN, 012 P-TURN)에서만 U턴할 수 있으므로, 그 밖의 U턴을 금지 경로로 만든다.
--   - U턴: 진입 링크의 끝 방향과 진출 링크의 시작 방향이 150도 이상 차이 나고, 진출 링크가 진입 링크의 교차로 그룹에서 시작
--   - 제외: U턴 허용 지점, 막다른 길(U턴 말고는 나갈 진출 링크가 없는 진입 링크)
--   - 금지 경로: 진입 링크 → 교차로 안 연결 링크(같은 그룹, 최단, 6개 이하) → 진출 링크
--     연결 링크가 7개 이상이면 나들목 같은 큰 구조일 수 있어 정상 경로를 막지 않도록 제외한다.
-- ============================================================

CREATE TABLE tb_road_uturn_prohibition (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    from_link_id        BIGINT NOT NULL,
    to_link_id          BIGINT NOT NULL,

    link_path           BIGINT[] NOT NULL,

    CONSTRAINT fk_road_uturn_prohibition_from_link
        FOREIGN KEY (from_link_id)
        REFERENCES tb_road_link(id),

    CONSTRAINT fk_road_uturn_prohibition_to_link
        FOREIGN KEY (to_link_id)
        REFERENCES tb_road_link(id),

    CONSTRAINT uk_road_uturn_prohibition_from_to
        UNIQUE (from_link_id, to_link_id)
);


-- 경로 탐색 대상 중 교차로 밖 링크의 진입·진출 방향(도). 진입 방향은 마지막 선분, 진출 방향은 첫 선분 기준
CREATE TEMP TABLE tmp_link_direction AS
SELECT id,
       start_node_id,
       end_node_id,
       left(start_node_id::TEXT, 8) AS start_group,
       left(end_node_id::TEXT, 8) AS end_group,
       degrees(ST_Azimuth(ST_PointN(geom, ST_NPoints(geom) - 1)::geography, ST_EndPoint(geom)::geography)) AS end_azimuth,
       degrees(ST_Azimuth(ST_StartPoint(geom)::geography, ST_PointN(geom, 2)::geography)) AS start_azimuth
FROM tb_road_link
WHERE is_routable AND NOT is_intersection_internal;

-- 진입 링크의 교차로 그룹에서 시작하는 진출 링크 쌍. turn_angle: 0=직진, 180=U턴
CREATE TEMP TABLE tmp_link_turn AS
SELECT a.id AS from_link_id,
       b.id AS to_link_id,
       a.end_node_id AS from_end_node_id,
       b.start_node_id AS to_start_node_id,
       abs(mod((b.start_azimuth - a.end_azimuth + 540)::NUMERIC, 360) - 180) AS turn_angle
FROM tmp_link_direction a
JOIN tmp_link_direction b ON b.start_group = a.end_group AND b.id <> a.id;

CREATE TEMP TABLE tmp_uturn AS
SELECT u.*
FROM tmp_link_turn u
WHERE u.turn_angle >= 150
  -- 막다른 길이 아님: U턴이 아닌 진출 링크가 있다
  AND EXISTS (SELECT 1 FROM tmp_link_turn o WHERE o.from_link_id = u.from_link_id AND o.turn_angle < 150)
  -- U턴 허용 지점이 아님
  AND NOT EXISTS (
      SELECT 1
      FROM tb_road_turn_restriction t
      WHERE t.turn_type_code IN ('011', '012')
        AND t.from_link_id = u.from_link_id
        AND t.to_link_id = u.to_link_id);

CREATE TEMP TABLE tmp_uturn_connector AS
SELECT start_vid, end_vid, array_agg(edge ORDER BY path_seq) FILTER (WHERE edge > 0) AS edges
FROM pgr_dijkstra(
    'SELECT id, start_node_id AS source, end_node_id AS target, length_m AS cost
     FROM tb_road_link
     WHERE is_routable AND is_intersection_internal',
    'SELECT DISTINCT from_end_node_id AS source, to_start_node_id AS target
     FROM tmp_uturn
     WHERE from_end_node_id <> to_start_node_id',
    directed => true)
GROUP BY start_vid, end_vid;

INSERT INTO tb_road_uturn_prohibition (from_link_id, to_link_id, link_path)
SELECT u.from_link_id,
       u.to_link_id,
       CASE WHEN u.from_end_node_id = u.to_start_node_id THEN ARRAY[u.from_link_id, u.to_link_id]
            ELSE ARRAY[u.from_link_id] || c.edges || u.to_link_id END
FROM tmp_uturn u
LEFT JOIN tmp_uturn_connector c ON c.start_vid = u.from_end_node_id AND c.end_vid = u.to_start_node_id
WHERE u.from_end_node_id = u.to_start_node_id OR cardinality(c.edges) <= 6
ORDER BY u.from_link_id, u.to_link_id;

DROP TABLE tmp_uturn_connector;
DROP TABLE tmp_uturn;
DROP TABLE tmp_link_turn;
DROP TABLE tmp_link_direction;


-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON TABLE tb_road_uturn_prohibition
    IS '허용 지점이 아닌 U턴. V16에서 차량 네트워크로부터 생성. 경로 탐색에서 회전 제한(금지)과 함께 쓴다';

COMMENT ON COLUMN tb_road_uturn_prohibition.from_link_id
    IS 'U턴을 시작하는 진입 링크 ID';

COMMENT ON COLUMN tb_road_uturn_prohibition.to_link_id
    IS 'U턴으로 빠져나가는 반대 방향 진출 링크 ID';

COMMENT ON COLUMN tb_road_uturn_prohibition.link_path
    IS '진입 링크부터 교차로 안 연결 링크를 거쳐 진출 링크까지의 링크 순서';
