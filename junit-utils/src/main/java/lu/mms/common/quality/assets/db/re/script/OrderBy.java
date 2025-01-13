package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Column;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderBy  extends DataQueryOperation {

    private Map<Column, OrderByType> columns;

    public OrderBy orderBy(final Column column, final Column... columns) {
        appendColumns(Stream.concat(Stream.ofNullable(column), Stream.ofNullable(columns).flatMap(Arrays::stream))
                .map(col -> Map.entry(col, OrderByType.NA))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        return this;
    }

    public OrderBy orderBy(final Column column, final OrderByType orderByType) {
        appendColumns(Map.of(column, orderByType));
        return this;
    }

    private void appendColumns(final Map<Column, OrderByType> columns) {
        if (this.columns == null) {
            this.columns = new LinkedHashMap<>();
        }
        this.columns.putAll(columns);
    }

    @Override
    public String build() {
        return Stream.of(
                        MapUtils.isEmpty(columns) ? StringUtils.EMPTY : "ORDER BY " + join(columns),
                        super.build()
                )
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String join(final Map<Column, OrderByType> columns) {
        return columns.entrySet().stream()
                .map(entry -> formatEntry(entry.getKey().build(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    private static String formatEntry(final String columName, final OrderByType orderByType) {
        final String value = orderByType != OrderByType.NA ? orderByType.name() : StringUtils.EMPTY;
        return String.format("%s %s", columName, value).trim();
    }

    public enum OrderByType {
        NA,
        ASC,
        DESC
    }
}
