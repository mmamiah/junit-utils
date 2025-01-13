package lu.mms.common.quality.assets.db.re.script;

import lu.mms.common.quality.assets.db.re.Function;
import lu.mms.common.quality.assets.db.re.Statement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Having extends OrderBy {

    private final SqlScript parent;
    private List<Statement> statements;

    public Having(final SqlScript parent) {
        this.parent = parent;
    }

    public OrderBy having(final Statement statement, final Statement... statements) {
        this.statements = Stream.concat(Stream.ofNullable(statement), Stream.ofNullable(statements).flatMap(Arrays::stream)).toList();
        return this;
    }

    @Override
    public String build() {
        return Stream.of(
                        this.parent.build(),
                        CollectionUtils.isEmpty(statements) ? StringUtils.EMPTY : "HAVING " + join(statements),
                        super.build()
                )
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String join(final List<Statement> statements) {
        return statements.stream().map(Statement::build).collect(Collectors.joining(" AND "));
    }
}
