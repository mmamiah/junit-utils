package lu.mms.common.quality.assets.db.oracle;

import lu.mms.common.quality.assets.db.MetadataFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OracleMetadata implements MetadataFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleMetadata.class);

    private static final String NOT_NULL = "NOT NULL";

    /* ------------------------- ORACLE -------------------------*/
    private static final String PRIMARY_KEY = "P";
    private static final String FOREIGN_KEY = "R";
    private static final String UNIQUE_KEY = "U";
    private static final String CHECK_KEY = "C";
    private static final String INFORMATION_SCHEMA_COLUMNS_SQL = StringUtils.EMPTY
            + "select * "
            + "from all_tab_columns "
            + "where OWNER = :schemaName and TABLE_NAME = :tableName";

    private static final String INFORMATION_SCHEMA_CONSTRAINTS_SQL = StringUtils.EMPTY
            + "select table_columns.column_name, cons.* "
            + "from all_constraints cons "
            + "join all_cons_columns table_columns on ( "
            + "    table_columns.constraint_name = cons.constraint_name"
            + "    and "
            + "    table_columns.table_name = cons.table_name"
            + ")"
            + "where cons.OWNER = :schemaName and cons.table_name = :tableName";

    private final Map<String, Map<String, Object>> columnsMetadata;
    private final List<Map<String, Object>> tableConstraintsMetadata;
    private final List<String> notNullConstraints;

    /**
     * Constructor.
     * @param dataSource The datasource
     * @param schema    The schema
     * @param tableName The table name
     */
    public OracleMetadata(final DataSource dataSource, final String schema, final String tableName) {
        final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        final SqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("schemaName", schema)
                .addValue("tableName", tableName);

        columnsMetadata = jdbcTemplate.queryForList(
                INFORMATION_SCHEMA_COLUMNS_SQL, sqlParams
        ).parallelStream()
                .flatMap(map -> Map.of(map.get("COLUMN_NAME"), map).entrySet().stream())
                .collect(Collectors.toMap(entry -> String.valueOf(entry.getKey()), Map.Entry::getValue));

        tableConstraintsMetadata = jdbcTemplate.queryForList(INFORMATION_SCHEMA_CONSTRAINTS_SQL, sqlParams);

        notNullConstraints = tableConstraintsMetadata.stream()
                .map(constraint -> String.valueOf(constraint.get("SEARCH_CONDITION")))
                .filter(condition -> condition.contains("NOT NULL"))
                .collect(Collectors.toList());
    }

    @Override
    public String getDatabaseType() {
        return "ORACLE";
    }

    @Override
    public String getKeyColumnId() {
        return "COLUMN_ID";
    }

    @Override
    public String getUniqueTableConstraint() {
        return getUniqueConstraint(tableConstraintsMetadata);
    }

    @Override
    public Map<String, Object> getColumnMetadata(final String columnName) {
        return columnsMetadata.get(columnName);
    }

    @Override
    public String getCheckConstraints(){
        return tableConstraintsMetadata.parallelStream()
                .filter(constraint -> String.valueOf(constraint.get("CONSTRAINT_TYPE")).equals(CHECK_KEY))
                .map(constraint -> String.valueOf(constraint.get("SEARCH_CONDITION")))
                .filter(condition -> !condition.contains(NOT_NULL))
                .reduce((a, b) -> String.join(" and ", a, b))
                .orElse(StringUtils.EMPTY);
    }

    @Override
    public String computeColumnType(final Map<String, Object> columnMetadata) {

        final String type = String.valueOf(columnMetadata.get("DATA_TYPE"));
        if (UNSIGNED_TYPE.contains(type) || type.contains("(") || type.contains(")")) {
            return appendNotNull(columnMetadata, type);
        }

        final Integer charMaxLength = parseInt(columnMetadata, "CHAR_LENGTH");

        if (NumberDataType.isNumeric(type)) {
            final Integer precision = parseInt(columnMetadata, "DATA_PRECISION");
            final Integer scale = parseInt(columnMetadata, "DATA_SCALE");
            return appendNotNull(columnMetadata, NumberDataType.format(precision, scale));
        } else if (charMaxLength != null && charMaxLength > 0) {
            return appendNotNull(columnMetadata, VarcharDataType.format(type, charMaxLength));
        }

        final Integer length = parseInt(columnMetadata, "DATA_LENGTH");
        if (length != null && length >= 0) {
            return appendNotNull(columnMetadata, String.format("%s(%s)", type, length));
        }
        return appendNotNull(columnMetadata, String.format("%s", type));

    }

    private String appendNotNull(final Map<String, Object> columnMetadata, final String value) {
        final String columnName = String.valueOf(columnMetadata.get("COLUMN_NAME"));
        final boolean isNotNull = notNullConstraints.parallelStream()
                .anyMatch(condition -> condition.contains(columnName));
        if (isNotNull) {
            return value.trim() + StringUtils.SPACE + NOT_NULL;
        }
        return value.trim();
    }

    private static String getUniqueConstraint(final List<Map<String, Object>> constraintSchema) {
        return constraintSchema.parallelStream()
                .filter(constraint -> String.valueOf(constraint.get("CONSTRAINT_TYPE")).equals(UNIQUE_KEY))
                .map(constraint -> String.valueOf(constraint.get("COLUMN_NAME")))
                .reduce((a, b) -> String.join(",", a, b))
                .orElse(StringUtils.EMPTY);
    }

}
