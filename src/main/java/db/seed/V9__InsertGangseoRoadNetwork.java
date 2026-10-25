package db.seed;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.PGConnection;

// ITS 표준노드링크(2026-10-14 배포) 중 강서구 범위 링크를 tb_road_node, tb_road_link에 적재한다.
//
// CSV는 원본 shapefile(MOCT_LINK)을 아래 명령으로 가공한 것이다. 링크만 있고, 노드는 링크 끝점에서 만든다.
//   - 범위: 강서구 도보 네트워크 범위 + 약 1km (경도 126.784~126.903, 위도 37.518~37.609)
//   - 좌표계: EPSG:5186 → EPSG:4326, 인코딩: CP949 → UTF-8
//   docker run --rm -e SHAPE_ENCODING=CP949 -v <원본 폴더>:/in:ro -v <출력 폴더>:/out ghcr.io/osgeo/gdal:alpine-small-latest \
//     ogr2ogr --config OGR_WKT_PRECISION 10 -f CSV -lco GEOMETRY=AS_WKT -nlt LINESTRING \
//     -s_srs EPSG:5186 -t_srs EPSG:4326 -spat 126.784 37.518 126.903 37.609 -spat_srs EPSG:4326 \
//     -select LINK_ID,F_NODE,T_NODE,LANES,ROAD_RANK,ROAD_TYPE,ROAD_NAME,ROAD_USE,CONNECT,MAX_SPD,REST_VEH,LENGTH \
//     /out/gangseo_road_network.csv /in/MOCT_LINK.shp
public class V9__InsertGangseoRoadNetwork extends BaseJavaMigration {

    private static final String CSV = "/db/data/gangseo_road_network.csv";

    // 컬럼 순서는 가공 CSV 헤더 순서와 같다.
    private static final String CREATE_STAGING = """
            CREATE TEMP TABLE stg_road_network (
                link_wkt        TEXT,
                link_id         TEXT,
                start_node_id   TEXT,
                end_node_id     TEXT,
                lanes           TEXT,
                road_rank       TEXT,
                road_type       TEXT,
                road_name       TEXT,
                road_use        TEXT,
                connect         TEXT,
                max_speed       TEXT,
                rest_veh        TEXT,
                link_length     TEXT
            )
            """;

    private static final String COPY_CSV =
            "COPY stg_road_network FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')";

    // 같은 노드 ID를 가진 링크 끝점은 좌표가 같다(원본 구축 기준). 그중 하나만 넣는다.
    private static final String INSERT_NODES = """
            INSERT INTO tb_road_node (id, geom)
            SELECT DISTINCT ON (e.id) e.id, e.geom
            FROM (
                SELECT start_node_id::BIGINT AS id, ST_StartPoint(ST_GeomFromText(link_wkt, 4326)) AS geom
                FROM stg_road_network
                UNION ALL
                SELECT end_node_id::BIGINT, ST_EndPoint(ST_GeomFromText(link_wkt, 4326))
                FROM stg_road_network
            ) e
            ORDER BY e.id
            """;

    private static final String INSERT_LINKS = """
            INSERT INTO tb_road_link (
                id, start_node_id, end_node_id, lanes, road_rank_code, road_type_code, road_name,
                is_in_use, is_ramp, restricted_vehicle_code, max_speed_kph, length_m, geom)
            SELECT
                link_id::BIGINT, start_node_id::BIGINT, end_node_id::BIGINT, NULLIF(lanes, '')::INTEGER,
                road_rank, road_type, NULLIF(road_name, ''),
                road_use = '0', connect = '1', rest_veh, max_speed::INTEGER, link_length::DOUBLE PRECISION,
                ST_GeomFromText(link_wkt, 4326)
            FROM stg_road_network
            """;

    // 범위를 사각형으로 잘라 경계 근처에는 들어가면 나올 수 없는 조각이 생긴다.
    // 승용차가 다닐 수 있는 링크로 강연결 요소를 구해 가장 큰 요소에 속한 링크만 경로 탐색 대상으로 표시한다.
    private static final String MARK_ROUTABLE = """
            WITH comp AS (
                SELECT component, node
                FROM pgr_strongComponents(
                    'SELECT id, start_node_id AS source, end_node_id AS target, travel_time_s AS cost
                     FROM tb_road_link
                     WHERE is_in_use AND restricted_vehicle_code <> ''1''')
            ), biggest AS (
                SELECT node
                FROM comp
                WHERE component = (SELECT component FROM comp GROUP BY component ORDER BY count(*) DESC LIMIT 1)
            )
            UPDATE tb_road_link l
            SET is_routable = TRUE
            FROM biggest s, biggest e
            WHERE l.start_node_id = s.node
              AND l.end_node_id = e.node
              AND l.is_in_use
              AND l.restricted_vehicle_code <> '1'
            """;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        try (Statement statement = connection.createStatement();
                InputStream csv =
                        Objects.requireNonNull(getClass().getResourceAsStream(CSV), "차량 네트워크 CSV가 없습니다: " + CSV)) {
            statement.execute(CREATE_STAGING);
            connection.unwrap(PGConnection.class).getCopyAPI().copyIn(COPY_CSV, csv);
            statement.execute(INSERT_NODES);
            statement.execute(INSERT_LINKS);
            statement.execute(MARK_ROUTABLE);
            statement.execute("DROP TABLE stg_road_network");
        }
    }
}
