package lu.mms.common.quality.junit.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public class Relation implements Comparable<Relation> {

    private static final String JOIN_TEMPLATE = "JOIN %s.%s %s ON %s.%s = %s.%s";

    private final String sourceColumn;
    private final String targetTable;
    private final String targetColumn;
    private final Join joinType;
    private String targetTableAlias;
    private String sourceTable;
    private String sourceTableAlias;


    private Relation(final Join joinType, final String columnName, final String targetTable, final String targetColumn) {
        this.joinType = joinType;
        this.sourceColumn = columnName;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }

    public Relation(final String columnName, final String targetTable, final String targetColumn) {
        this((Join) null, columnName, targetTable, targetColumn);
    }

    public Relation(final String sourceTableName, final String columnName, final String targetTable, final String targetColumn) {
        this(columnName, targetTable, targetColumn);
        this.sourceTable = sourceTableName;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public void setSourceTableAlias(String sourceTableAlias) {
        this.sourceTableAlias = sourceTableAlias;
    }

    public String getSourceTableAlias() {
        return sourceTableAlias;
    }

    public void setTargetTableAlias(String targetTableAlias) {
        this.targetTableAlias = targetTableAlias;
    }

    public String getTargetTableAlias() {
        return targetTableAlias;
    }

    /**
     * The SQL 'Join' clause template.
     *
     * @param columnName the column name
     * @param targetTable the target table to join
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql join clause template
     */
    public static Relation join(final String columnName, final String targetTable, final String targetColumn) {
        return new Relation(columnName, targetTable, targetColumn);
    }

    /**
     * The SQL 'LEFT Join' clause template.
     *
     * @param columnName the column name
     * @param targetTable the target table to join
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql left join clause template
     */
    public static Relation leftJoin(final String columnName, final String targetTable, final String targetColumn) {
        return new Relation(Join.LEFT, columnName, targetTable, targetColumn);
    }

    /**
     * The SQL 'RIGHT Join' clause template.
     *
     * @param columnName the column name
     * @param targetTable the target table to join
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql right join clause template
     */
    public static Relation rightJoin(final String columnName, final String targetTable, final String targetColumn) {
        return new Relation(Join.RIGHT, columnName, targetTable, targetColumn);
    }

    /**
     * The SQL 'FULL Join' clause template.
     *
     * @param columnName the column name
     * @param targetTable the target table to join
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql full join clause template
     */
    public static Relation fullJoin(final String columnName, final String targetTable, final String targetColumn) {
        return new Relation(Join.FULL, columnName, targetTable, targetColumn);
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public Join getJoinType() {
        return joinType;
    }

    public String build(final Table table) {
        // resolve the columns
        final Column columnOne = table.getSchema()
                .getTables().get(sourceTable)
                .getColumns().get(getSourceColumn());
        final Column columnTwo = table.getSchema()
                .getTables().get(getTargetTable())
                .getColumns().get(getTargetColumn());
        final Column sourceColumn;
        final Column foreignColumn;
        if (table.getName().equals(columnOne.getParentTable().getName())) {
            sourceColumn = columnTwo;
            foreignColumn = columnOne;
        } else {
            sourceColumn = columnOne;
            foreignColumn = columnTwo;
        }

        final String prefix = this.joinType == null ? StringUtils.EMPTY : this.joinType.name() + StringUtils.SPACE;

        this.sourceTableAlias = sourceColumn.getParentTable().getAlias();
        this.targetTableAlias = foreignColumn.getParentTable().getAlias();

        // build the SQL expression
        return String.format(
                prefix + JOIN_TEMPLATE,
                sourceColumn.getParentTable().getSchema().getName(),
                sourceColumn.getParentTable().getName(),
                this.sourceTableAlias,
                this.sourceTableAlias,
                sourceColumn.getName(),
                this.targetTableAlias,
                foreignColumn.getName()
        );
    }

    @Override
    public int compareTo(final Relation relation) {
        if (getStraightComparator().compare(this, relation) == 0) {
            return 0;
        }
        return getCrossComparator().compare(this, relation);
    }

    public static Comparator<Relation> getStraightComparator() {
        return Comparator.comparing(Relation::getSourceTable)
                .thenComparing(Relation::getSourceColumn)
                .thenComparing(Relation::getTargetTable)
                .thenComparing(Relation::getTargetColumn);
    }

    public static Comparator<Relation> getCrossComparator() {
        return (one, two) -> {
            if (!one.getSourceTable().equals(two.getTargetTable())) {
                return -1;
            } else if (!one.getSourceColumn().equals(two.getTargetColumn())) {
                return 1;
            }
            return 0;
        };
    }
}
