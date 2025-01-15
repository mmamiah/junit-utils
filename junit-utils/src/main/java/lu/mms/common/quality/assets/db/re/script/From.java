package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.CanBuild;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

public class From implements SqlScript {

    /**
     * Retrieve all entries.
     */
    private static final String SELECT_FROM_STATEMENT = StringUtils.EMPTY
            + "SELECT %s \n"
            + "FROM %s.%s %s";

    private final CanBuild[] arguments;
    private Table table;

    From(final CanBuild... arguments) {
        this.arguments = arguments;
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
        final String columnStr = Stream.ofNullable(arguments)
                .flatMap(Arrays::stream)
                .map(CanBuild::build)
                .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                .orElse(StringUtils.EMPTY);
        // build the SQL depending on 'values' are empty or not
        return String.format(
                SELECT_FROM_STATEMENT,
                columnStr,
                table.getSchema().getName(),
                table.getName(),
                table.computeAlias()
        );
    }

    @Override
    public String toString() {
        return build();
    }
}
