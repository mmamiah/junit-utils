package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.Statement;
import lu.mms.common.quality.assets.db.re.schema.Column;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Where extends GroupBy {

    /**
     * Retrieve all entries.
     */
    private final From from;
    private final Set<String> joints = new HashSet<>();

    private String whereExpression;

    public Where(From from) {
        this.from = from;
    }

    public Where join(final Relation... relations) {
        assert relations != null : "'relations' must not be null";
        return join(Arrays.asList(relations));
    }

    public Where join(final Collection<Relation> relations) {
        for (final Relation relation : new HashSet<>(relations)) {
            joints.add(relation.build(this.from.getTable()));
        }
        return this;
    }

    /**
     * The SQL 'Join' clause template.
     *
     * @param columnName   the column name
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql join clause template
     */
    public Where  join(final String columnName, final Column targetColumn) {
        joints.add(new Relation(columnName, targetColumn.getParentTable().getName(), targetColumn.getName()).build(this.from.getTable()));
        return this;
    }

    /**
     * The SQL 'LEFT Join' clause template.
     *
     * @param columnName   the column name
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql left join clause template
     */
    public Where  leftJoin(final String columnName, final Column targetColumn) {
        joints.add(new Relation(JoinType.LEFT, columnName, targetColumn.getParentTable().getName(), targetColumn.getName()).build(this.from.getTable()));
        return this;
    }

    /**
     * The SQL 'RIGHT Join' clause template.
     *
     * @param columnName   the column name
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql right join clause template
     */
    public Where  rightJoin(final String columnName, final Column targetColumn) {
        joints.add(new Relation(JoinType.RIGHT, columnName, targetColumn.getParentTable().getName(), targetColumn.getName()).build(this.from.getTable()));
        return this;
    }

    /**
     * The SQL 'FULL Join' clause template.
     *
     * @param columnName   the column name
     * @param targetColumn the target column to join (defined in 'targetTable')
     * @return the sql full join clause template
     */
    public Where  fullJoin(final String columnName, final Column targetColumn) {
        joints.add(new Relation(JoinType.FULL, columnName, targetColumn.getParentTable().getName(), targetColumn.getName()).build(this.from.getTable()));
        return this;
    }

    public GroupBy where(final Statement... statements) {
        return where(statements!= null ? Arrays.asList(statements) : List.of());
    }

    public GroupBy where(final Collection<Statement> statements) {
        this.whereExpression = new HashSet<>(statements).stream()
                .map(Statement::build)
                .reduce((a, b) -> String.format("%s and %s", a, b))
                .orElse(StringUtils.EMPTY);
        return this;
    }

    @Override
    public String build() {
        // Collect the build from parent element
        return Stream.of(
                        from.build(),
                        joints.stream().collect(Collectors.joining(System.lineSeparator())),
                        StringUtils.isNotBlank(whereExpression) ? "WHERE " + whereExpression : StringUtils.EMPTY,
                        super.build()
        )
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(System.lineSeparator()));
    }
}
