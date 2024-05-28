package lu.mms.common.quality.junit.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Data Query Language.
 */
public class Dml implements MigrationScript {

    private static final String DATA_FILENAME_FORMAT = "data_%s.sql";

    private static final String DELETE_RECORDS_BANNER =  StringUtils.LF
            + "--------------------------------------------- ----------------------------------------\n"
            + "------------------------------------- DELETE RECORDS ---------------------------------\n"
            + "--------------------------------------------- ----------------------------------------\n";

    private static final String DELETE_RECORDS = StringUtils.EMPTY
            + "DELETE FROM  %s.%s;\n";

    private static final String INSERT_INTO_BANNER = StringUtils.LF
            + "--------------------------------------------- ----------------------------------------\n"
            + "------------------------------------- INSERT RECORDS ---------------------------------\n"
            + "--------------------------------------------- ----------------------------------------\n";

    private static final String INSERT_INTO = StringUtils.EMPTY
            + "INSERT INTO %s.%s (\n"
            + "\t(%s) \n"
            + "VALUES \n"
            + "%s \n"
            + ";\n\n";

    private final Schema schema;
    private final Class<?> packageProvider;

    private Dml(final Schema schema, final Class<?> packageProvider) {
        this.schema = schema;
        this.packageProvider = packageProvider;
    }

    public static Dml with(final Schema schema) {
        return new Dml(schema, null);
    }

    public static Dml with(final Schema schema, final Class<?> packageProvider) {
        return new Dml(schema, packageProvider);
    }

    /**
     * Create the DML from the provided {@code tables} in the <i>/SQL</i> folder with the rest resources folder..
     */
    @Override
    public boolean createFile() {
        final String execTime = schema.getExecutionTime();
        final String banner = MigrationScript.prepareBanner(execTime);
        final String dml = build();
        final String filename = String.format(
                DATA_FILENAME_FORMAT,
                NOW_DATETIME_FORMATTER.format(LocalDateTime.now())
        );
        return MigrationScript.createFile(packageProvider, filename, List.of(banner, dml));
    }

    /**
     * @return  The DML string
     */
    @Override
    public String build() {
        final Collection<Table> tables = schema.getTables().values();
        final String deleteRecordsSql = tables.stream()
                .sorted()
                .map(table -> String.format(DELETE_RECORDS, table.getSchema().getName(), table.getName()))
                .reduce(StringUtils::join)
                .orElse(StringUtils.EMPTY);
        final String insertRecordsSql = tables.stream()
                .sorted(Comparator.reverseOrder())
                .map(Dml::mapTableToInsertDML)
                .reduce(StringUtils::join)
                .orElse(StringUtils.EMPTY);
        return StringUtils.join(DELETE_RECORDS_BANNER, deleteRecordsSql, INSERT_INTO_BANNER, insertRecordsSql);
    }

    private static String mapTableToInsertDML(final Table table) {
        // collect the column name (comma separated)
        final String columnsSql = table.getColumns().keySet().stream()
                .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                .orElse(StringUtils.EMPTY);
        return table.getRecords().stream()
                // For each record, collect the concatenated (comma separated) column values
                .map(record -> table.getColumns().keySet().stream()
                        .map(columnName -> String.valueOf(record.getValue(columnName)))
                        .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                        .map(a -> String.format("\t(%s)", a))
                        .orElse(StringUtils.EMPTY))
                .reduce((a, b) -> StringUtils.joinWith(",\n", a, b))
                .map(dataSql -> String.format(INSERT_INTO, table.getSchema().getName(), table.getName(), columnsSql, dataSql))
                .orElse(StringUtils.EMPTY);
    }
}
