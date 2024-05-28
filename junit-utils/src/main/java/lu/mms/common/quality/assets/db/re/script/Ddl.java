package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Definition Language.
 */
public class Ddl implements MigrationScript {

    private static final String SCHEMA_FILENAME_FORMAT = "schema_%s.sql";

    private static final String CREATE_SCHEMA =  StringUtils.LF
            + "--------------------------------------------- ---------------------------------------\n"
            + "---------------------------------------- CREATE SCHEMA ------------------------------\n"
            + "--------------------------------------------- ---------------------------------------\n"
            + "CREATE SCHEMA IF NOT EXISTS %s;"
            + "\n\n";

    private static final String DROP_TABLES_BANNER =  StringUtils.LF
            + "--------------------------------------------- ---------------------------------------\n"
            + "-------------------------------------- DROP TABLES ----------------------------------\n"
            + "--------------------------------------------- ---------------------------------------\n";

    private static final String DROP_TABLE = "DROP TABLE %s.%s IF EXISTS; --%s\n";

    private static final String CREATE_TABLES_BANNER =  StringUtils.LF
            + "--------------------------------------------- ----------------------------------------\n"
            + "-------------------------------------- CREATE TABLE ----------------------------------\n"
            + "--------------------------------------------- ----------------------------------------\n";

    private static final String CREATE_TABLE = StringUtils.EMPTY
            + "CREATE TABLE %s.%s (\n"
            + "%s"
            + ");\n\n";

    /** Table column definition. example: DT_VALEUR VARCHAR2(100 CHAR) */
    private static final String TABLE_COLUMN = "\t%s %s\n";

    private static final String PK_TEMPLATE = ",\tCONSTRAINT PK_%s PRIMARY KEY (%s)\n";
    private static final String FK_TEMPLATE = ",\tCONSTRAINT FK_%s FOREIGN KEY (%s) REFERENCES %s(%s)\n";
    private static final String UNIQUE_TEMPLATE = ",\tCONSTRAINT UC_%s UNIQUE (%s)\n";
    private static final String CHECK_TEMPLATE = ",\tCONSTRAINT CHK_%s CHECK (%s)\n";

    private final Schema schema;
    private final Class<?> packageProvider;

    private Ddl(final Schema schema, final Class<?> packageProvider) {
        this.schema = schema;
        this.packageProvider = packageProvider;
    }

    public static Ddl with(final Schema schema) {
        return new Ddl(schema, null);
    }

    public static Ddl with(final Schema schema, final Class<?> packageProvider) {
        return new Ddl(schema, packageProvider);
    }

    /**
     * Create the DDL from the provided {@code schema} in the same package that the {@code packageProvider}.
     */
    @Override
    public boolean createFile() {
        final String banner = MigrationScript.prepareBanner(schema.getExecutionTime());
        final String ddl = build();
        final String filename = String.format(
                SCHEMA_FILENAME_FORMAT,
                NOW_DATETIME_FORMATTER.format(LocalDateTime.now())
        );
        return MigrationScript.createFile(packageProvider, filename, List.of(banner, ddl));
    }

    /**
     * @return  The DDL string
     */
    @Override
    public String build() {
        final String schemaSql = String.format(CREATE_SCHEMA, schema.getName());
        final String dropTablesSql = schema.getTables().values().stream()
                .map(table -> String.format(DROP_TABLE, schema.getName(), table.getName(), table.getDescription()))
                .reduce(StringUtils::join)
                .orElse(StringUtils.EMPTY);
        final String createTableSql = schema.getTables().values().stream()
                .sorted(Comparator.reverseOrder())
                .map(Ddl::mapTableToDDL)
                .reduce(StringUtils::join)
                .orElse(StringUtils.EMPTY);
        return StringUtils.join(schemaSql, DROP_TABLES_BANNER, dropTablesSql, CREATE_TABLES_BANNER, createTableSql);
    }

    private static String mapTableToDDL(final Table table) {
        final String columnsSql = table.getColumns().values().stream()
                .map(column -> String.format(TABLE_COLUMN, column.getName(), column.getDefinition()))
                .reduce((a, b) -> StringUtils.joinWith(",", a, b))
                .orElse(StringUtils.EMPTY);

        String tablePK = StringUtils.EMPTY;
        if (!table.getPrimaryKeyConstraint().isBlank()) {
            tablePK = String.format(PK_TEMPLATE, table.getName(), table.getPrimaryKeyConstraint());
        }

        String tableFK = StringUtils.EMPTY;
        if (!table.getForeignKeys().isEmpty()) {
            final AtomicInteger index = new AtomicInteger(0);
            tableFK = table.getForeignKeys().entrySet().stream()
                    .flatMap(entry -> entry.getValue().parallelStream().map(ref -> Map.entry(entry.getKey(), ref)))
                    .map(entry -> String.format(
                            FK_TEMPLATE,
                            table.getName() + "_" + index.incrementAndGet(),
                            entry.getKey(),
                            entry.getValue().getTargetTable(),
                            entry.getValue().getTargetColumn())
                    )
                    .reduce(StringUtils::join)
                    .orElse(StringUtils.EMPTY);
        }

        String uniqueConstraint = StringUtils.EMPTY;
        if (!table.getUniqueConstraint().isBlank()) {
            uniqueConstraint = String.format(UNIQUE_TEMPLATE, table.getName(), table.getUniqueConstraint());
        }

        String checkConstraint = StringUtils.EMPTY;
        if (!table.getCheckConstraint().isBlank()) {
            checkConstraint = String.format(CHECK_TEMPLATE, table.getName(), table.getCheckConstraint());
        }

        final String tableDdl = StringUtils.join(columnsSql, tablePK, tableFK, uniqueConstraint, checkConstraint);
        return String.format(CREATE_TABLE, table.getSchema().getName(), table.getName(), tableDdl);
    }
}
