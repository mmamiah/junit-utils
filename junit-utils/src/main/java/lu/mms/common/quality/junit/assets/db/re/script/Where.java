package lu.mms.common.quality.junit.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.Statement;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class Where extends DataQueryOperation implements SqlScript {

    /**
     * Retrieve all entries.
     */
    private static final String STATEMENT_LINE = "%s \n";

    private final From from;
    private final Set<String> joints = new HashSet<>();

    private String whereExpression;

    public Where(From from) {
        this.from = from;
    }

    public Where join(final Set<Relation> relations){
        for (final Relation relation : relations) {
            joints.add(relation.build(this.from.getTable()));
        }
        return this;
    }

    public DataQueryOperation where(final Set<Statement> statements) {
        this.whereExpression = statements.stream()
                .map(Statement::build)
                .reduce((a, b) -> String.format("%s and %s", a, b))
                .orElse(StringUtils.EMPTY);
        return this;
    }

    @Override
    public String build() {
        // select # from
        StringBuilder whereBuilder = new StringBuilder(String.format(STATEMENT_LINE, from.toString()));

        // join
        for (final String join : joints) {
            whereBuilder.append(String.format(STATEMENT_LINE, join));
        }

        if (StringUtils.isNotBlank(whereExpression)) {
            whereBuilder.append(String.format(STATEMENT_LINE, "WHERE " + whereExpression));
        }

        whereBuilder.append(getDqoExpression());

        // where
        return whereBuilder.toString();
    }
}
