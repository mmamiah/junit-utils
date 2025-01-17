package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.re.schema.Column;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See: <a href="https://www.sqltutorial.org/sql-functions/">...</a>
 */
public class Function implements CanBuild {

    private String name;
    private Column[] columns;

    public Function() {
        this.columns = null;
    }

    public static Function fn() {
        return new Function();
    }

    // SQL Aggregate Functions
    public Expression min(final Column column){
        this.name = "MIN";
        this.columns = new Column[]{column};
        return new Expression(this);
    }

    public Expression max(final Column column){
        this.name = "MAX";
        this.columns = new Column[]{column};
        return new Expression(this);
    }

    public Expression count(final Column column){
        this.name = "COUNT";
        this.columns = new Column[]{column};
        return new Expression(this);
    }

    public Expression sum(final Column column){
        this.name = "SUM";
        this.columns = new Column[]{column};
        return new Expression(this);
    }

    public Expression avg(final Column column){
        this.name = "AVG";
        this.columns = new Column[]{column};
        return new Expression(this);
    }

    // SQL String Functions
    public Expression concat(final Column columnOne, final Column columnTwo, final Column... columns){
        this.name = "CONCAT";
        this.columns = Stream.concat(
                Stream.concat(Stream.ofNullable(columnOne), Stream.ofNullable(columnTwo)),
                Stream.ofNullable(columns).flatMap(Arrays::stream)
        ).toArray(Column[]::new);
        return new Expression(this);
    }

    @Override
    public String build() {
        return formatFunction(this.name, columns);
    }

    @Override
    public String toString() {
        return this.build();
    }

    private static String formatFunction(final String functionName, final Column... columns){
        final String args = Stream.ofNullable(columns).flatMap(Arrays::stream)
                .map(col -> String.format("%s.%s", col.getParentTable().computeAlias(), col.getName()))
                .collect(Collectors.joining(","));
        return String.format("%s(%s)", functionName, args);
    }
}
