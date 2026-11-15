package com.all4land.parkinglotnavigator.domain.route;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class RoadNetworkDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadsGangseoRoadNetwork() {
        // 노드는 링크 끝점에서 만든다
        assertThat(count("tb_road_node")).isEqualTo(7462);
        assertThat(count("tb_road_link")).isEqualTo(10297);
    }

    @Test
    void routableLinksAreUsableAndFormLargestStrongComponent() {
        // 통행 가능 10,188개 중 가장 큰 강연결 요소에 속한 링크
        assertThat(count("tb_road_link WHERE is_routable")).isEqualTo(9186);
        assertThat(count("tb_road_link WHERE is_routable AND (NOT is_in_use OR restricted_vehicle_code = '1')"))
                .isZero();

        Long components = jdbcTemplate.queryForObject("""
                SELECT count(DISTINCT component)
                FROM pgr_strongComponents(
                    'SELECT id, start_node_id AS source, end_node_id AS target, travel_time_s AS cost
                     FROM tb_road_link WHERE is_routable')
                """, Long.class);
        assertThat(components).isEqualTo(1);
    }

    @Test
    void loadsNodeAttributes() {
        assertThat(count("tb_road_node WHERE node_type_code IS NOT NULL")).isEqualTo(7462);
        assertThat(count("tb_road_node WHERE has_turn_restriction")).isEqualTo(602);
    }

    @Test
    void loadsTurnRestrictionsWithConnectedLinkPaths() {
        // 진입·진출 링크가 모두 차량 네트워크에 있는 회전정보
        assertThat(count("tb_road_turn_restriction")).isEqualTo(672);
        // 금지 유형(101, 102, 103) 중 경로 탐색 대상 링크로 이어지는 것.
        // 나머지는 진입·진출 링크가 탐색 대상이 아니거나, 교차로 안 연결 링크가 없어 원래 갈 수 없는 회전이다.
        assertThat(count("tb_road_turn_restriction WHERE link_path IS NOT NULL"
                        + " AND turn_type_code IN ('101', '102', '103', '003', '002')"))
                .isEqualTo(198);

        // link_path는 진입 링크로 시작해 진출 링크로 끝나고, 앞 링크의 종료 노드에서 다음 링크가 시작한다
        assertThat(count("tb_road_turn_restriction WHERE link_path IS NOT NULL"
                        + " AND (link_path[1] <> from_link_id OR link_path[cardinality(link_path)] <> to_link_id)"))
                .isZero();
        assertThat(count("""
                        tb_road_turn_restriction r
                        CROSS JOIN LATERAL generate_subscripts(r.link_path, 1) i
                        JOIN tb_road_link a ON a.id = r.link_path[i]
                        JOIN tb_road_link b ON b.id = r.link_path[i + 1]
                        WHERE a.end_node_id <> b.start_node_id
                        """)).isZero();
    }

    @Test
    void marksIntersectionInternalLinks() {
        // 양 끝 노드가 같은 교차로 그룹(노드 ID 앞 8자리)에 속한 링크
        assertThat(count("tb_road_link WHERE is_routable AND is_intersection_internal"))
                .isEqualTo(3189);
    }

    @Test
    void travelTimeIsLengthOverSpeedLimit() {
        assertThat(count("tb_road_link WHERE abs(travel_time_s - length_m / (max_speed_kph / 3.6)) > 1e-9"))
                .isZero();
    }

    private long count(String from) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM " + from, Long.class);
    }
}
