package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.re.schema.Column;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See: <a href="https://www.sqltutorial.org/sql-functions/">...</a>
 */
public class Function extends Expression {

    private String functionName;
    private Column columnTwo;
    private Column[] columns;

    public Function() {
        super(null);
        this.columnTwo = null;
        this.columns = null;
    }

    public static Function fn() {
        return new Function();
    }

    // SQL Aggregate Functions
    public Expression min(final Column column){
        this.functionName = "MIN";
        this.setColumn(column);
        return this;
    }

    public Expression max(final Column column){
        this.functionName = "MAX";
        this.setColumn(column);
        return this;
    }

    public Expression count(final Column column){
        this.functionName = "COUNT";
        this.setColumn(column);
        return this;
    }

    public Expression sum(final Column column){
        this.functionName = "SUM";
        this.setColumn(column);
        return this;
    }

    public Expression avg(final Column column){
        this.functionName = "AVG";
        this.setColumn(column);
        return this;
    }

    // SQL String Functions
    public Expression concat(final Column columnOne, final Column columnTwo, final Column... columns){
        this.functionName = "CONCAT";
        this.setColumn(columnOne);
        this.columnTwo = columnTwo;
        this.columns = columns;
        return this;
    }

    @Override
    public String getPrefix() {
        return formatFunction(functionName,
                this.getColumn(),
                Stream.concat(
                        Stream.ofNullable(columnTwo),
                        Stream.ofNullable(columns).flatMap(Arrays::stream)
                ).toArray(Column[]::new)
        );
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.functionName, super.toString());
    }

    private static String formatFunction(final String functionName, final Column column, final Column... columns){
        final String args = Stream.concat(Stream.of(column),  Stream.ofNullable(columns).flatMap(Arrays::stream))
                .map(col -> String.format("%s.%s", col.getParentTable().getAlias(), col.getName()))
                .collect(Collectors.joining(","));
        return String.format("%s(%s)", functionName, args);
    }
}
