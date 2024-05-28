package lu.mms.common.quality.junit.assets.db.db2;

import lu.mms.common.quality.assets.db.MetadataFactory;
import lu.mms.common.quality.assets.db.oracle.NumberDataType;
import lu.mms.common.quality.assets.db.oracle.VarcharDataType;
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

public class DB2Metadata implements MetadataFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DB2Metadata.class);

    private static final String NOT_NULL = "NOT NULL";

    /* ------------------------- DB2 -------------------------*/
    private static final String PRIMARY_KEY = "P";
    private static final String UNIQUE_KEY = "U";
    private static final String CHECK_KEY = "C";
//    private static final String NOT_NULL_KEY = "N";
//    private static final String FOREIGN_KEY = "R";
//    private static final String TABLE_KEY = "T";
    private static final String INFORMATION_SCHEMA_COLUMNS_SQL = StringUtils.EMPTY
            + "select * "
            + "from SYSIBM.SYSCOLUMNS "
            + "where TBCREATOR = :schemaName and TBNAME = :tableName";

    private static final String INFORMATION_SCHEMA_CHECKS_SQL = StringUtils.EMPTY+
            "select columns.TBNAME, checks.* " +
                    "from SYSIBM.SYSCOLUMNS columns " +
                    "join SYSIBM.SYSCHECKDEP scdep on (" +
                    "       scdep.TBOWNER = columns.TBCREATOR " +
                    "       and " +
                    "       scdep.TBNAME = columns.TBNAME" +
                    "       and " +
                    "       scdep.COLNAME = columns.NAME" +
                    "   ) " +
                    "join SYSIBM.SYSCHECKS checks on (" +
                    "       checks.TBOWNER = scdep.TBOWNER " +
                    "       and " +
                    "       checks.TBNAME = scdep.TBNAME" +
                    "       and " +
                    "       checks.CHECKNAME = scdep.CHECKNAME" +
                    "   ) " +
            "where columns.TBCREATOR = :schemaName and columns.TBNAME = :tableName";

    private static final String INFORMATION_SCHEMA_CONSTRAINTS_SQL = StringUtils.EMPTY
            + "select tableConst.* "
            + "from SYSIBM.SYSTABCONST tableConst "
            + "where tableConst.TBCREATOR = :schemaName and tableConst.TBNAME = :tableName";

    private final Map<String, Map<String, Object>> columnsMetadata;
    private final List<Map<String, Object>> tableConstraintsMetadata;
    private final List<Map<String, Object>> tableChecksMetadata;
    private final List<String> notNullConstraints;

    /**
     * Constructor.
     * @param dataSource The datasource
     * @param schema    The schema
     * @param tableName The table name
     */
    public DB2Metadata(final DataSource dataSource, final String schema, final String tableName) {
        final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        final SqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("schemaName", schema)
                .addValue("tableName", tableName);

        columnsMetadata = jdbcTemplate.queryForList(
                INFORMATION_SCHEMA_COLUMNS_SQL, sqlParams
        ).parallelStream()
                .flatMap(map -> Map.of(map.get("NAME"), map).entrySet().stream())
                .collect(Collectors.toMap(entry -> String.valueOf(entry.getKey()), Map.Entry::getValue));

        tableConstraintsMetadata = jdbcTemplate.queryForList(INFORMATION_SCHEMA_CONSTRAINTS_SQL, sqlParams);


        tableChecksMetadata = jdbcTemplate.queryForList(INFORMATION_SCHEMA_CHECKS_SQL, sqlParams);

        notNullConstraints = tableConstraintsMetadata.stream()
                .map(constraint -> String.valueOf(constraint.get("SEARCH_CONDITION")))
                .filter(condition -> condition.contains("NOT NULL"))
                .collect(Collectors.toList());
    }

    @Override
    public String getDatabaseType() {
        return "DB2";
    }

    @Override
    public String getKeyColumnId() {
        return "COLNO";
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
    public String getKeyColumnDefault() {
        return "DEFAULTVALUE";
    }

    @Override
    public String getCheckConstraints(){
        return tableChecksMetadata.parallelStream()
                .map(constraint -> String.valueOf(constraint.get("CHECKCONDITION")))
                .reduce((a, b) -> String.join(" and ", a, b))
                .orElse(StringUtils.EMPTY);
    }

    @Override
    public String computeColumnType(final Map<String, Object> columnMetadata) {

        final String type = String.valueOf(columnMetadata.get("COLTYPE")).trim();
        if (UNSIGNED_TYPE.contains(type) || type.contains("(") || type.contains(")")) {
            return appendNotNull(columnMetadata, type);
        }

        final Integer charMaxLength = parseInt(columnMetadata, "LENGTH");

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
                .filter(constraint -> String.valueOf(constraint.get("TYPE")).equals(UNIQUE_KEY))
                .map(constraint -> String.valueOf(constraint.get("CONSTNAME")))
                .reduce((a, b) -> String.join(",", a, b))
                .orElse(StringUtils.EMPTY);
    }

}
