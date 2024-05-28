package lu.mms.common.quality.junit.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class From implements SqlScript {

    /**
     * Retrieve all entries.
     */
    private static final String SELECT_FROM_STATEMENT = StringUtils.EMPTY
            + "SELECT %s \n"
            + "FROM %s.%s %s";

    private final Collection<Column> columns;
    private Table table;

    From(final Collection<Column> columns) {
        this.columns = columns;
    }

    public Where from(final Table table) {
        this.table = table;
        return new Where(this);
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String build() {
        final String columnStr = columns.stream()
                .map(column -> String.join(".", table.getAlias(), column.getName()))
                .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                .orElse(StringUtils.EMPTY);
        // build the SQL depending on 'values' are empty or not
        return String.format(
                SELECT_FROM_STATEMENT,
                columnStr,
                table.getSchema().getName(),
                table.getName(),
                table.getAlias()
        );
    }

    @Override
    public String toString() {
        return build();
    }
}
