package lu.mms.common.quality.assets.db.h2;

import lu.mms.common.quality.assets.db.DBDriverName;
import lu.mms.common.quality.assets.db.MetadataFactory;
import lu.mms.common.quality.assets.db.oracle.NumberDataType;
import lu.mms.common.quality.assets.db.oracle.VarcharDataType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class H2Metadata implements MetadataFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Metadata.class);

    private static final String MODE_SQL = StringUtils.EMPTY
            + "select SETTING_VALUE "
            + "from INFORMATION_SCHEMA.SETTINGS "
            + "where SETTING_NAME = 'MODE'";

//    private static final String NUMBER_

    private final Map<String, Map<String, Object>> columnsMetadata;
    private final List<Map<String, Object>> tableConstraintsMetadata;

    /* ------------------------- H2 -------------------------*/
    private static final String INFORMATION_SCHEMA_COLUMNS_SQL = StringUtils.EMPTY
            + "select * "
            + "from INFORMATION_SCHEMA.COLUMNS "
            + "where TABLE_SCHEMA = :schemaName and TABLE_NAME = :tableName";

    private static final String INFORMATION_SCHEMA_CONSTRAINTS_SQL = StringUtils.EMPTY
            + "select tc.*, ck.CHECK_CLAUSE "
            + "from INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc "
            + "left join INFORMATION_SCHEMA.CHECK_CONSTRAINTS ck on ck.CONSTRAINT_NAME = tc.CONSTRAINT_NAME "
            + "where tc.CONSTRAINT_SCHEMA = :schemaName and tc.TABLE_NAME = :tableName";

    /**
     * For future use.<br>
     * The idea is to identify the H2 DB mode in order for a best match of the migration script.
     */
    private final DBDriverName mode;

    public H2Metadata(final DataSource dataSource, final String schema, final String tableName) {
        mode = getCompatibilityMode(dataSource);
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
    }

    @Override
    public String getDatabaseType() {
        return "H2";
    }

    @Override
    public String getKeyColumnId() {
        return "ORDINAL_POSITION";
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
                .filter(constraint -> String.valueOf(constraint.get("CONSTRAINT_TYPE")).equals("CHECK"))
                .map(constraint -> String.valueOf(constraint.get("CHECK_CLAUSE")))
                .reduce((a, b) -> String.join(" and ", a, b))
                .orElse(StringUtils.EMPTY);
    }

    @Override
    public String computeColumnType(final Map<String, Object> columnMetadata) {
        final String type = String.valueOf(columnMetadata.get("DATA_TYPE"));
        if (UNSIGNED_TYPE.contains(type) || type.contains("(") || type.contains(")")) {
            return type;
        }

        final Integer charMaxLength = parseInt(columnMetadata, "CHARACTER_MAXIMUM_LENGTH");

        if (NumberDataType.isNumeric(type)) {
            final Integer precision = parseInt(columnMetadata, "NUMERIC_PRECISION");
            final Integer scale = parseInt(columnMetadata, "NUMERIC_SCALE");
            return NumberDataType.format(precision, scale);
        } else if (charMaxLength != null && charMaxLength > 0) {
            return VarcharDataType.format(type, charMaxLength);
        }

        final Integer length = parseInt(columnMetadata, "DATA_LENGTH");
        if (length != null && length >= 0) {
            return String.format("%s(%s)", type, length);
        }
        return String.format("%s", type);
    }

    private static String getUniqueConstraint(final List<Map<String, Object>> constraintSchema) {
        return constraintSchema.parallelStream()
                .filter(constraint -> String.valueOf(constraint.get("CONSTRAINT_TYPE")).equals("UNIQUE"))
                .map(constraint -> String.valueOf(constraint.get("COLUMN_LIST")))
                .reduce((a, b) -> String.join(",", a, b))
                .orElse(StringUtils.EMPTY);
    }

    private static DBDriverName getCompatibilityMode(final DataSource dataSource) {
        final String mode = new JdbcTemplate(dataSource)
                .queryForObject(MODE_SQL, String.class);

        return DBDriverName.valueOf(StringUtils.defaultString(mode).trim().toUpperCase());
    }
}
