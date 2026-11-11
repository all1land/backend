package com.all4land.parkinglotnavigator.domain.commoncode.repository;

import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeCreateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeResDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeUpdateReqDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommonCodeRepository {

    private static final String COLUMNS = """
            id, parent_id, code, name, level, sort_order, description, state, created_at, updated_at
            """;

    private static final RowMapper<CommonCodeResDTO> ROW_MAPPER = CommonCodeRepository::mapRow;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<CommonCodeResDTO> findRoots(boolean includeInactive) {
        String sql = "SELECT " + COLUMNS + " FROM tb_cmn_code WHERE parent_id IS NULL"
                + (includeInactive ? "" : " AND state = 'ACTIVE'") + " ORDER BY sort_order ASC, id ASC";
        return jdbcTemplate.query(sql, Map.of(), ROW_MAPPER);
    }

    public Optional<CommonCodeResDTO> findRootByCode(String rootCode) {
        List<CommonCodeResDTO> results = jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM tb_cmn_code WHERE parent_id IS NULL AND code = :code",
                Map.of("code", rootCode),
                ROW_MAPPER);
        return results.stream().findFirst();
    }

    public List<CommonCodeResDTO> findChildren(Long parentId, boolean includeInactive) {
        String sql = "SELECT " + COLUMNS + " FROM tb_cmn_code WHERE parent_id = :parentId"
                + (includeInactive ? "" : " AND state = 'ACTIVE'") + " ORDER BY sort_order ASC, id ASC";
        return jdbcTemplate.query(sql, Map.of("parentId", parentId), ROW_MAPPER);
    }

    public Optional<CommonCodeResDTO> findById(Long id) {
        List<CommonCodeResDTO> results = jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM tb_cmn_code WHERE id = :id", Map.of("id", id), ROW_MAPPER);
        return results.stream().findFirst();
    }

    public boolean existsByParentAndCode(Long parentId, String code, Long excludedId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM tb_cmn_code"
                + " WHERE parent_id IS NOT DISTINCT FROM CAST(:parentId AS BIGINT) AND code = :code"
                + (excludedId == null ? "" : " AND id <> :excludedId") + ")";
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("parentId", parentId)
                .addValue("code", code);
        if (excludedId != null) {
            parameters.addValue("excludedId", excludedId);
        }
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    public boolean hasChildren(Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM tb_cmn_code WHERE parent_id = :id)", Map.of("id", id), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    public boolean isReferenced(String rootCode, String code) {
        String sql = switch (rootCode) {
            case "PLACE_CATEGORY" -> "SELECT EXISTS (SELECT 1 FROM tb_place WHERE category_code = :code)";
            case "PARKING_CATEGORY" ->
                    "SELECT EXISTS (SELECT 1 FROM tb_parking_lot WHERE category_code = :code)";
            case "PARKING_TYPE" ->
                    "SELECT EXISTS (SELECT 1 FROM tb_parking_lot WHERE parking_type_code = :code)";
            case "USER_ROLE" -> "SELECT EXISTS (SELECT 1 FROM tb_user WHERE role = :code)";
            default -> null;
        };
        if (sql == null) {
            return false;
        }
        Boolean exists = jdbcTemplate.queryForObject(sql, Map.of("code", code), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    public Long insert(CommonCodeCreateReqDTO request, int level) {
        String sql = """
                INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
                VALUES (:parentId, :code, :name, :level, :sortOrder, :description, :state)
                RETURNING id
                """;
        return jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("parentId", request.parentId())
                        .addValue("code", request.code())
                        .addValue("name", request.name())
                        .addValue("level", level)
                        .addValue("sortOrder", request.sortOrder() == null ? 0 : request.sortOrder())
                        .addValue("description", request.description())
                        .addValue("state", request.state() == null ? "ACTIVE" : request.state()),
                Long.class);
    }

    public void update(Long id, CommonCodeUpdateReqDTO request) {
        String sql = """
                UPDATE tb_cmn_code
                   SET code = :code, name = :name, sort_order = :sortOrder,
                       description = :description, state = :state, updated_at = LOCALTIMESTAMP
                 WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("code", request.code())
                        .addValue("name", request.name())
                        .addValue("sortOrder", request.sortOrder())
                        .addValue("description", request.description())
                        .addValue("state", request.state()));
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM tb_cmn_code WHERE id = :id", Map.of("id", id));
    }

    private static CommonCodeResDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CommonCodeResDTO(
                rs.getLong("id"),
                rs.getObject("parent_id", Long.class),
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("level"),
                rs.getInt("sort_order"),
                rs.getString("description"),
                rs.getString("state"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
