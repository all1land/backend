package com.all4land.parkinglotnavigator.drivingroute;

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
    void travelTimeIsLengthOverSpeedLimit() {
        assertThat(count("tb_road_link WHERE abs(travel_time_s - length_m / (max_speed_kph / 3.6)) > 1e-9"))
                .isZero();
    }

    private long count(String from) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM " + from, Long.class);
    }
}
