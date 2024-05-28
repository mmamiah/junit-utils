package lu.mms.common.quality.junit.assets.db.re.schema;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;

import java.util.Comparator;

/**
 * The DB table column definition.
 */
public class Column implements Comparable<Column>, Cloneable  {

    private static final String NOT_NULL = " NOT NULL";
    private static final String UNIQUE = "UNIQUE";

    private Table parentTable;
    private final int columnId;
    private final String name;
    private final String columnType;
    private final boolean autoIncrement;
    private final Object defaultValue;
    private boolean primaryKey;
    private boolean unique;

    private Object value;

    public Column(final int columnId, final String name, final String columnType, final boolean primaryKey, final boolean unique,
                  final boolean autoIncrement, final Object defaultValue) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The column name can not be blank or empty.");
        }
        this.columnId = columnId;
        this.name = name.toUpperCase();
        this.columnType = columnType;
        this.primaryKey = primaryKey;
        this.unique = unique;
        this.autoIncrement = autoIncrement;
        this.defaultValue = defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public Column withValue(final Object value) {
        this.value = value;
        return this;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setParent(Table parentTable) {
        this.parentTable = parentTable;
    }

    public Table getParentTable() {
        return parentTable;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        final StringBuilder definition = new StringBuilder(columnType);
        if (isPrimaryKey()) {
            final int startIndex = definition.indexOf(NOT_NULL);
            if (startIndex >= 0) {
                final int endIndex = startIndex + NOT_NULL.length();
                definition.replace(startIndex, endIndex, StringUtils.EMPTY);
            }
            definition.append(StringUtils.SPACE).append("PRIMARY KEY");
            return definition.toString();
        }
        if (isUnique()) {
            definition.append(StringUtils.SPACE).append(UNIQUE);
            return definition.toString();
        }

        final String defaultStr = formattedDefault(defaultValue);
        if (StringUtils.isNotBlank(defaultStr)) {
            definition.append(StringUtils.SPACE).append("DEFAULT").append(StringUtils.SPACE).append(defaultStr);
        }
        return definition.toString();
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setNotPrimaryKey() {
        this.primaryKey = false;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setNotUnique() {
        this.unique = false;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    private static String formattedDefault(final Object defaultValue) {
        if (!(defaultValue instanceof String)) {
            return String.valueOf(ObjectUtils.defaultIfNull(defaultValue, StringUtils.EMPTY));
        } else if (StringUtils.isNotBlank((String) defaultValue)) {
            return String.format("'%s'", defaultValue);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final Column column = new Column(this.columnId, this.name, this.columnType, this.primaryKey, this.unique,
                this.autoIncrement, this.defaultValue
        );
        new ModelMapper().map(this, column);
        return column;
    }

    @Override
    public int compareTo(final Column o) {
        final Comparator<Column> comparator = Comparator.nullsLast(
                Comparator.comparing(Column::getName)
                .thenComparing(column -> String.valueOf(column.getValue()))
        );

        return comparator.compare(this, o);
    }

    @Override
    public String toString() {
        return "Column: " + getName() + " " + getDefinition() + StringUtils.SPACE;
    }
}
