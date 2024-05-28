package lu.mms.common.quality.junit.assets.db.re.schema;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class represent a database entry.
 */
public class Record {

    private static final Set<String> TEMPORAL_CLASS = Set.of("TEMPORAL", "TIMESTAMP");

    /**
     * The record values: [Column, Value]
     */
    private final Map<String, Object> values;

    public Record() {
        this.values = new HashMap<>();
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Object getValue(final String column) {
        return values.get(column);
    }

    /**
     * Add a new value to the record.
     * @param columnName    The column name
     * @param value The column value
     */
    public void appendColumnValue(final String columnName, final Object value) {
        Object formattedValue = value;
        final boolean isCharSeq = value != null &&
                (value instanceof String || TEMPORAL_CLASS.contains(value.getClass().getSimpleName().toUpperCase()));
        if (isCharSeq) {
            formattedValue = String.valueOf(value)
                    .replace("'", "\\'")
                    .replace("\n", StringUtils.EMPTY);
            formattedValue  = String.format("'%s'", formattedValue);
        }
        values.put(columnName, formattedValue);
    }

    public Object getColumnValue(final String columnName) {
        return values.get(columnName.toUpperCase());
    }

    /**
     * Compare two records (their values map) and determine if the are equal.
     * @param record The record to compare to.
     * @return  true,   if the values map are the same amount of record, and if all entries match. <br>
     *          false,  otherwise.
     */
    boolean equalTo(final Record record) {
        if (values.size() != record.getValues().size()) {
            return false;
        }
        return values.entrySet().stream()
                .allMatch(entry -> Objects.equals(record.getColumnValue(entry.getKey()), entry.getValue()));
    }

    @Override
    public String toString() {
        return "Record{" +
                values.entrySet().stream()
                        .filter(entry -> entry.getValue() != null)
                        .map(entry -> StringUtils.joinWith("=", entry.getKey(), entry.getValue()))
                        .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                        .orElse(StringUtils.EMPTY)
                + '}';
    }
}
