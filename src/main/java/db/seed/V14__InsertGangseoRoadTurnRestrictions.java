package db.seed;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.PGConnection;

// ITS 표준노드링크(2026-10-14 배포)의 노드 속성과 회전정보를 V9 차량 네트워크에 맞춰 적재한다.
//
// CSV는 원본 dbf(MOCT_NODE, TURNINFO)를 아래 명령으로 CSV(UTF-8)로 바꾼 뒤, gangseo_road_network.csv에 있는 노드와
// 링크(진입·진출 링크 모두)에 해당하는 행만 남긴 것이다. 회전정보의 비고(REMARK) 컬럼은 비어 있어 뺐다.
//   docker run --rm -e SHAPE_ENCODING=CP949 -v <원본 폴더>:/in:ro -v <출력 폴더>:/out ghcr.io/osgeo/gdal:alpine-small-latest \
//     ogr2ogr -f CSV /out/turninfo_all.csv /in/TURNINFO.dbf
//   docker run --rm -e SHAPE_ENCODING=CP949 -v <원본 폴더>:/in:ro -v <출력 폴더>:/out ghcr.io/osgeo/gdal:alpine-small-latest \
//     ogr2ogr -f CSV -spat 126.77 37.51 126.92 37.62 -spat_srs EPSG:4326 -select NODE_ID,NODE_TYPE,NODE_NAME,TURN_P \
//     /out/node_area.csv /in/MOCT_NODE.shp
public class V14__InsertGangseoRoadTurnRestrictions extends BaseJavaMigration {

    private static final String NODE_CSV = "/db/data/gangseo_road_node.csv";
    private static final String TURN_CSV = "/db/data/gangseo_road_turn.csv";

    private static final String CREATE_NODE_STAGING = """
            CREATE TEMP TABLE stg_road_node (
                node_id         TEXT,
                node_type       TEXT,
                node_name       TEXT,
                turn_p          TEXT
            )
            """;

    private static final String CREATE_TURN_STAGING = """
            CREATE TEMP TABLE stg_road_turn (
                node_id         TEXT,
                turn_id         TEXT,
                st_link         TEXT,
                ed_link         TEXT,
                turn_type       TEXT,
                turn_oper       TEXT
            )
            """;

    private static final String COPY_NODE_CSV =
            "COPY stg_road_node FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')";

    private static final String COPY_TURN_CSV =
            "COPY stg_road_turn FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')";

    private static final String UPDATE_NODES = """
            UPDATE tb_road_node n
            SET node_type_code = s.node_type,
                node_name = NULLIF(s.node_name, ''),
                has_turn_restriction = s.turn_p = '1'
            FROM stg_road_node s
            WHERE n.id = s.node_id::BIGINT
            """;

    private static final String INSERT_TURNS = """
            INSERT INTO tb_road_turn_restriction (
                node_id, turn_seq, from_link_id, to_link_id, turn_type_code, operation_code)
            SELECT node_id::BIGINT, turn_id::INTEGER, st_link::BIGINT, ed_link::BIGINT, turn_type, turn_oper
            FROM stg_road_turn
            """;

    // 교차로는 접근 방향마다 노드가 따로 있고(노드 ID 앞 8자리가 같은 그룹), 그 노드들을 교차로 안 연결 링크가 잇는다.
    // 우회전은 진입 링크 종료 노드에서 진출 링크가 바로 시작하지만, 좌회전·직진은 연결 링크를 거친다.
    // 같은 그룹 안의 경로 탐색 대상 링크만으로 진입 링크 종료 노드 → 진출 링크 시작 노드 최단 경로를 구해 링크 순서를 만든다.
    // 연결 링크가 4개 이상이면 교차로 안 이동이 아니라 다른 도로를 거치는 경로라 보고 비워 둔다(나들목 등).
    private static final String MARK_LINK_PATH = """
            WITH turn AS (
                SELECT t.id, t.from_link_id, t.to_link_id, f.end_node_id AS from_end, e.start_node_id AS to_start
                FROM tb_road_turn_restriction t
                JOIN tb_road_link f ON f.id = t.from_link_id
                JOIN tb_road_link e ON e.id = t.to_link_id
                WHERE f.is_routable AND e.is_routable
            ), connector AS (
                SELECT start_vid, end_vid, array_agg(edge ORDER BY path_seq) FILTER (WHERE edge > 0) AS edges
                FROM pgr_dijkstra(
                    'SELECT id, start_node_id AS source, end_node_id AS target, length_m AS cost
                     FROM tb_road_link
                     WHERE is_routable AND left(start_node_id::TEXT, 8) = left(end_node_id::TEXT, 8)',
                    'SELECT DISTINCT f.end_node_id AS source, e.start_node_id AS target
                     FROM tb_road_turn_restriction t
                     JOIN tb_road_link f ON f.id = t.from_link_id
                     JOIN tb_road_link e ON e.id = t.to_link_id
                     WHERE f.is_routable AND e.is_routable AND f.end_node_id <> e.start_node_id',
                    directed => true)
                GROUP BY start_vid, end_vid
            )
            UPDATE tb_road_turn_restriction r
            SET link_path = CASE
                    WHEN t.from_end = t.to_start THEN ARRAY[t.from_link_id, t.to_link_id]
                    ELSE ARRAY[t.from_link_id] || c.edges || t.to_link_id
                END
            FROM turn t
            LEFT JOIN connector c ON c.start_vid = t.from_end AND c.end_vid = t.to_start
            WHERE r.id = t.id
              AND (t.from_end = t.to_start OR cardinality(c.edges) <= 3)
            """;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        try (Statement statement = connection.createStatement();
                InputStream nodeCsv = open(NODE_CSV);
                InputStream turnCsv = open(TURN_CSV)) {
            statement.execute(CREATE_NODE_STAGING);
            statement.execute(CREATE_TURN_STAGING);
            connection.unwrap(PGConnection.class).getCopyAPI().copyIn(COPY_NODE_CSV, nodeCsv);
            connection.unwrap(PGConnection.class).getCopyAPI().copyIn(COPY_TURN_CSV, turnCsv);
            statement.execute(UPDATE_NODES);
            statement.execute(INSERT_TURNS);
            statement.execute(MARK_LINK_PATH);
            statement.execute("DROP TABLE stg_road_node");
            statement.execute("DROP TABLE stg_road_turn");
        }
    }

    private InputStream open(String path) {
        return Objects.requireNonNull(getClass().getResourceAsStream(path), "차량 네트워크 CSV가 없습니다: " + path);
    }
}
