package db.seed;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.PGConnection;

// 서울시 자치구별 도보 네트워크 공간정보(강서구) CSV를 tb_walk_node, tb_walk_link에 적재한다.
// 원본 CSV(CP949)를 그대로 임시 테이블에 COPY한 뒤 SQL로 변환한다. 데이터 갱신 시 CSV만 교체하고 새 버전으로 재적재한다.
public class V7__InsertGangseoWalkNetwork extends BaseJavaMigration {

    private static final String CSV = "/db/data/gangseo_walk_network.csv";

    // 컬럼 순서는 원본 CSV 헤더 순서와 같다.
    private static final String CREATE_STAGING = """
            CREATE TEMP TABLE stg_walk_network (
                record_type     TEXT,
                node_wkt        TEXT,
                node_id         TEXT,
                node_type_code  TEXT,
                link_wkt        TEXT,
                link_id         TEXT,
                link_type_code  TEXT,
                start_node_id   TEXT,
                end_node_id     TEXT,
                link_length     TEXT,
                sgg_code        TEXT,
                sgg_name        TEXT,
                emd_code        TEXT,
                emd_name        TEXT,
                elevated        TEXT,
                subway_network  TEXT,
                bridge          TEXT,
                tunnel          TEXT,
                overpass        TEXT,
                crosswalk       TEXT,
                park            TEXT,
                in_building     TEXT,
                collected_at    TEXT
            )
            """;

    // UHC는 PostgreSQL에서 CP949를 가리키는 인코딩 이름
    private static final String COPY_CSV =
            "COPY stg_walk_network FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UHC')";

    private static final String INSERT_NODES = """
            INSERT INTO tb_walk_node (id, node_type_code, geom)
            SELECT node_id::BIGINT, NULLIF(node_type_code, ''), ST_GeomFromText(node_wkt, 4326)
            FROM stg_walk_network
            WHERE record_type = 'NODE'
            """;

    // 구 경계 밖으로 이어지는 링크의 끝 노드는 원본에 없어서, 링크 선형의 끝점 좌표로 만든다.
    private static final String INSERT_BOUNDARY_NODES = """
            INSERT INTO tb_walk_node (id, geom)
            SELECT DISTINCT ON (e.id) e.id, e.geom
            FROM (
                SELECT start_node_id::BIGINT AS id, ST_StartPoint(ST_GeomFromText(link_wkt, 4326)) AS geom
                FROM stg_walk_network
                WHERE record_type = 'LINK'
                UNION ALL
                SELECT end_node_id::BIGINT, ST_EndPoint(ST_GeomFromText(link_wkt, 4326))
                FROM stg_walk_network
                WHERE record_type = 'LINK'
            ) e
            WHERE NOT EXISTS (SELECT 1 FROM tb_walk_node n WHERE n.id = e.id)
            ORDER BY e.id
            """;

    private static final String INSERT_LINKS = """
            INSERT INTO tb_walk_link (
                id, start_node_id, end_node_id, link_type_code, length_m,
                is_elevated, is_subway_network, is_bridge, is_tunnel,
                is_overpass, is_crosswalk, is_park, is_in_building,
                geom)
            SELECT
                link_id::BIGINT, start_node_id::BIGINT, end_node_id::BIGINT, link_type_code, link_length::DOUBLE PRECISION,
                elevated = '1', subway_network = '1', bridge = '1', tunnel = '1',
                overpass = '1', crosswalk = '1', park = '1', in_building = '1',
                ST_GeomFromText(link_wkt, 4326)
            FROM stg_walk_network
            WHERE record_type = 'LINK'
            """;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        try (Statement statement = connection.createStatement();
                InputStream csv =
                        Objects.requireNonNull(getClass().getResourceAsStream(CSV), "도보 네트워크 CSV가 없습니다: " + CSV)) {
            statement.execute(CREATE_STAGING);
            connection.unwrap(PGConnection.class).getCopyAPI().copyIn(COPY_CSV, csv);
            statement.execute(INSERT_NODES);
            statement.execute(INSERT_BOUNDARY_NODES);
            statement.execute(INSERT_LINKS);
            statement.execute("DROP TABLE stg_walk_network");
        }
    }
}
