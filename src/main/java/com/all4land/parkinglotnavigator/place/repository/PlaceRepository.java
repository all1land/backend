package com.all4land.parkinglotnavigator.place.repository;

import com.all4land.parkinglotnavigator.place.dto.PlaceListRequest;
import com.all4land.parkinglotnavigator.place.dto.PlaceResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceRepository {

    // 공간 인덱스로 후보를 좁힌 뒤 실제 경위도로 경계를 검증한다. 경계선 위의 장소도 포함한다.
    private static final String CONDITIONS = """
            FROM tb_place p
            WHERE p.deleted_at IS NULL
              AND p.location::geometry && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
              AND ST_Y(p.location::geometry) BETWEEN :minLat AND :maxLat
              AND ST_X(p.location::geometry) BETWEEN :minLon AND :maxLon
              AND (:keyword = '' OR strpos(lower(p.name), lower(:keyword)) > 0
                   OR strpos(lower(p.road_name_address), lower(:keyword)) > 0)
            """;

    private static final String LIST_SQL = """
            SELECT p.id, p.name, p.road_name_address, p.lot_number_address,
                   ST_Y(p.location::geometry) AS latitude,
                   ST_X(p.location::geometry) AS longitude,
                   ST_Y(p.entrance_location::geometry) AS entrance_latitude,
                   ST_X(p.entrance_location::geometry) AS entrance_longitude
            """ + CONDITIONS + " ORDER BY p.id ASC LIMIT :limit OFFSET :offset";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public long countInBounds(PlaceListRequest request) {
        return jdbcTemplate.queryForObject("SELECT count(*) " + CONDITIONS, parameters(request), Long.class);
    }

    public List<PlaceResponse> findInBounds(PlaceListRequest request) {
        var parameters = new MapSqlParameterSource(parameters(request))
                .addValue("limit", request.getPageSize())
                .addValue("offset", (long) (request.getPageIndex() - 1) * request.getPageSize());
        return jdbcTemplate.query(
                LIST_SQL,
                parameters,
                (rs, rowNum) -> new PlaceResponse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("road_name_address"),
                        rs.getString("lot_number_address"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getObject("entrance_latitude", Double.class),
                        rs.getObject("entrance_longitude", Double.class)));
    }

    private Map<String, Object> parameters(PlaceListRequest request) {
        return Map.of(
                "minLat",
                request.getMinLat(),
                "minLon",
                request.getMinLon(),
                "maxLat",
                request.getMaxLat(),
                "maxLon",
                request.getMaxLon(),
                "keyword",
                request.getKeyword());
    }
}
