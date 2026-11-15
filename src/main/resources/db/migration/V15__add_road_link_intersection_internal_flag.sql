-- ============================================================
-- ROAD LINK INTERSECTION INTERNAL FLAG
-- 교차로는 접근 방향마다 노드가 따로 있고(노드 ID 앞 8자리가 같은 그룹), 그 노드들을 교차로 안 연결 링크가 잇는다.
-- 우회전은 연결 링크를 지나지 않고, 직진은 1개, 좌회전은 2개를 지난다.
-- ============================================================

ALTER TABLE tb_road_link
    ADD COLUMN is_intersection_internal BOOLEAN
        GENERATED ALWAYS AS (left(start_node_id::TEXT, 8) = left(end_node_id::TEXT, 8)) STORED;

COMMENT ON COLUMN tb_road_link.is_intersection_internal
    IS '교차로 안 연결 링크 여부. 양 끝 노드 ID 앞 8자리(교차로 그룹)가 같다. 투영 대상에서 빼고 교차로 신호 대기 시간을 더하는 데 쓴다';
