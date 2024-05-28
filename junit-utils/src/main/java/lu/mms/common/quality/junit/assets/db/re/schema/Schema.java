package lu.mms.common.quality.junit.assets.db.re.schema;

import org.springframework.util.StopWatch;

import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class Schema {

    private final String name;
    private final Map<String, Table> tables;
    private String executionTime;

    public Schema(final String schemaName) {
        this.tables = new HashMap<>();
        this.name = schemaName;
    }

    public void addTable(final Table table) {
        if (tables.containsKey(table.getName())) {
            return;
        }
        table.setSchema(this);
        this.tables.put(table.getName(), table);
    }

    /**
     * @return The Schema name
     */
    public String getName() {
        return name;
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public Table appendTable(final String tableName, final DatabaseMetaData metadata,
                             final BiConsumer<DatabaseMetaData, Table> dataDefinition) {
        // retrieve the target table
        final Table targetTable;
        if (tables.containsKey(tableName)) {
            targetTable = tables.get(tableName);
        } else {
            targetTable = new Table(tableName);
            addTable(targetTable);
            // enrich the table with data definition
            dataDefinition.accept(metadata, targetTable);
        }
        return targetTable;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void cleanConstraints() {
        for (final Table table: tables.values()) {
            table.cleanConstraints();
        }
    }

    public List<Object> getColumnValues(final String tableName, final String columnName) {
        return Optional.ofNullable(tables.get(tableName.toUpperCase()))
                .map(table -> table.getValue(columnName))
                .orElseThrow(() -> new IllegalArgumentException(String.format("The table [%s] or column [%s] do not exists.", tableName, columnName)));
    }

    public void setElapsedTime(final StopWatch stopWatch) {
        final long minutes = TimeUnit.MINUTES.convert(stopWatch.getLastTaskTimeNanos(), TimeUnit.NANOSECONDS);
        final long seconds = TimeUnit.SECONDS.convert(stopWatch.getLastTaskTimeNanos(), TimeUnit.NANOSECONDS);
        final long millis = stopWatch.getLastTaskTimeMillis();
        if (minutes == 0) {
            this.executionTime = String.format("%s sec %s ms.",  seconds, millis);
        } else {
            this.executionTime = String.format("%s min, %s sec %s ms.", minutes, seconds, millis);
        }
    }

    @Override
    public String toString() {
        return "Schema{" +
                "name='" + name + '\'' +
                ", tables=" + tables +
                '}';
    }
}
