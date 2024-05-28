package lu.mms.common.quality.assets.db;

import java.util.Map;
import java.util.Set;

public interface MetadataFactory {

    Set<String> UNSIGNED_TYPE = Set.of("DATE");

    default Integer parseInt(final Map<String, Object> columnMetadata, final String key) {
        final Object value = columnMetadata.get(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    String getDatabaseType();

    String getUniqueTableConstraint();

    Map<String, Object> getColumnMetadata(final String columnName);

    String getCheckConstraints();

    String computeColumnType(final Map<String, Object> columnMetadata);

    String getKeyColumnId();

    default String getKeyColumnDefault() {
        return "COLUMN_DEFAULT";
    }

}
