package com.all4land.parkinglotnavigator.walkingroute;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class WalkNetworkDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadsGangseoWalkNetwork() {
        // 원본 노드 7,947개 + 구 경계 밖 노드 113개
        assertThat(count("tb_walk_node")).isEqualTo(8060);
        assertThat(count("tb_walk_link")).isEqualTo(10856);
    }

    @Test
    void largestComponentCoversAlmostWholeNetwork() {
        Long largest = jdbcTemplate.queryForObject("""
                SELECT max(cnt) FROM (
                    SELECT count(*) AS cnt
                    FROM pgr_connectedComponents(
                        'SELECT id, start_node_id AS source, end_node_id AS target, length_m AS cost FROM tb_walk_link')
                    GROUP BY component
                ) c
                """, Long.class);

        assertThat(largest).isEqualTo(8038);
    }

    private long count(String table) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);
    }
}
