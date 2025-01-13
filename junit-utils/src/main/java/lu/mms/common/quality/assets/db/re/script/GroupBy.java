package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Column;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupBy extends OrderBy {

    private List<Column> columns;

    public Having groupBy(final Column column, final Column... columns) {
        this.columns = Stream.concat(Stream.ofNullable(column), Stream.ofNullable(columns).flatMap(Arrays::stream)).toList();
        return new Having(this);
    }

    @Override
    public String build() {
        return Stream.of(
                        CollectionUtils.isEmpty(columns) ? StringUtils.EMPTY : "GROUP BY " + join(columns),
                        super.build()
                )
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String join(final List<Column> columns) {
        return columns.stream()
                .map(Column::build)
                .collect(Collectors.joining(","));
    }
}

