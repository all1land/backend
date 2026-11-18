package db.seed;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.PGConnection;

// 서울 열린데이터광장 강서구 인허가 장소 데이터를 적재한다.
public class V17__InsertAdditionalGangseoPlaces extends BaseJavaMigration {
    private static final String CSV = "/db/data/gangseo_additional_places.csv";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        try (Statement statement = connection.createStatement();
                InputStream csv = Objects.requireNonNull(getClass().getResourceAsStream(CSV), "장소 CSV가 없습니다: " + CSV)) {
            statement.execute("""
                    UPDATE tb_cmn_code SET name = CASE code WHEN 'HOSPITAL' THEN '병의원' WHEN 'CAFE' THEN '카페·디저트' END
                    WHERE code IN ('HOSPITAL', 'CAFE') AND parent_id = (SELECT id FROM tb_cmn_code WHERE parent_id IS NULL AND code = 'PLACE_CATEGORY');

                    INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
                    SELECT p.id, v.code, v.name, 2, v.sort_order, NULL, 'ACTIVE'
                    FROM tb_cmn_code p CROSS JOIN (VALUES
                        ('PHARMACY', '약국', 10),
                        ('ANIMAL_HOSPITAL', '동물병원', 11),
                        ('SHOPPING', '쇼핑·마트', 12),
                        ('SUPERMARKET', '대형마트', 13),
                        ('DEPARTMENT_STORE', '백화점', 14),
                        ('CONVENIENCE_STORE', '편의점', 15),
                        ('BEAUTY', '미용', 16),
                        ('CULTURE', '문화시설', 17),
                        ('ACCOMMODATION', '숙박시설', 18),
                        ('LEISURE', '여가시설', 19),
                        ('SPORTS', '운동시설', 20),
                        ('BATH', '목욕시설', 21),
                        ('TEMPLE', '사찰', 22)
                    ) AS v(code, name, sort_order)
                    WHERE p.parent_id IS NULL AND p.code = 'PLACE_CATEGORY'
                    ON CONFLICT (parent_id, code) DO NOTHING;
                    """);
            statement.execute("""
                    CREATE TEMP TABLE gangseo_place_seed (
                        source_dataset text, source_id text, name text, category_code text,
                        road_name_address text, lot_number_address text, zip_code text, phone text,
                        x double precision, y double precision,
                        category_name text, latitude text, longitude text
                    ) ON COMMIT DROP
                    """);
            connection
                    .unwrap(PGConnection.class)
                    .getCopyAPI()
                    .copyIn("COPY gangseo_place_seed FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')", csv);
            statement.execute("""
                    DO $$
                    BEGIN
                        IF (SELECT count(*) FROM gangseo_place_seed) <> 11098 THEN
                            RAISE EXCEPTION 'Expected 11098 source places';
                        END IF;
                        IF EXISTS (
                            SELECT 1 FROM gangseo_place_seed s
                            CROSS JOIN LATERAL (SELECT ST_Transform(ST_SetSRID(ST_MakePoint(s.x, s.y), 5174), 4326) AS geom) p
                            WHERE NOT (ST_Y(p.geom) BETWEEN 37.50 AND 37.65 AND ST_X(p.geom) BETWEEN 126.70 AND 126.95)
                               OR NOT (s.road_name_address LIKE '서울특별시 강서구 %' OR s.road_name_address LIKE '서울 강서구 %')
                               OR NOT EXISTS (
                                   SELECT 1 FROM tb_cmn_code c JOIN tb_cmn_code root ON root.id = c.parent_id
                                   WHERE root.parent_id IS NULL AND root.code = 'PLACE_CATEGORY'
                                     AND c.code = s.category_code AND c.state = 'ACTIVE'
                               )
                        ) THEN
                            RAISE EXCEPTION 'Invalid source coordinates, address, or category';
                        END IF;
                    END $$;

                    INSERT INTO tb_place (location, name, category_code, road_name_address, lot_number_address, zip_code, phone)
                    SELECT ST_Transform(ST_SetSRID(ST_MakePoint(s.x, s.y), 5174), 4326)::geography,
                           s.name, s.category_code, s.road_name_address, s.lot_number_address, s.zip_code, s.phone
                    FROM gangseo_place_seed s
                    WHERE NOT EXISTS (
                        SELECT 1 FROM tb_place p
                        WHERE p.road_name_address = s.road_name_address AND p.name = s.name
                    )
                    ON CONFLICT (road_name_address, name) DO NOTHING;
                    """);
        }
    }
}
