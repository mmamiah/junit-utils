package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.re.schema.Column;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * The SQL Expression.
 */
public class Expression implements Comparable<Expression>, Operator<Expression>, Condition<Expression> {

    private static final String EXP_TMPL = "(%s)";

    private CanBuild parent;
    private Column column;
    private String expression = EMPTY;
    private String alias;

    protected Expression() {
        this.column = null;
        this.parent = null;
    }

    protected Expression(final CanBuild parent) {
        this.parent = parent;
    }

    protected Expression( final Column column) {
        this.column = column;
    }

    public static Expression property(final Column column) {
        return new Expression(column);
    }

    public static Expression property(final String columnName) {
        return new Expression(new Column(columnName));
    }

    String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = StringUtils.isBlank(expression) ? EMPTY : expression;
    }

    public Column getColumn() {
        return column;
    }

    protected void setColumn(final Column column) {
        this.column = column;
    }

    @Deprecated
    public String getAlias() {
        return alias;
    }

    @Deprecated
    @Override
    public void applyAlias(final String alias) {
        this.alias = alias;
        if (expression.contains(".")) {
            return;
        }
        expression = String.format("%s.%s", alias, expression);
    }

    @Override
    public Expression and(final Statement statement) {
        this.expression = String.format("%s and %s", appendParenthesis(this.expression), appendParenthesis(statement.build()));
        return this;
    }

    @Override
    public Expression or(final Statement statement) {
        this.expression = String.format("%s or %s", appendParenthesis(this.expression), appendParenthesis(statement.build()));
        return this;
    }

    @Override
    public Operator<Expression> not(Statement statement) {
        this.expression = String.format("%s not %s", appendParenthesis(this.expression), appendParenthesis(statement.build()));
        return this;
    }

    @Override
    public Operator<Expression> eq(final Object value) {
        setExpression(buildUnaryExpression("=", value));
        return this;
    }

    @Override
    public Operator<Expression> gt(final Object value) {
        setExpression(buildUnaryExpression(">", value));
        return this;
    }

    @Override
    public Operator<Expression> ge(final Object value) {
        setExpression(buildUnaryExpression(">=", value));
        return this;
    }

    @Override
    public Operator<Expression> lt(final Object value) {
        setExpression(buildUnaryExpression("<", value));
        return this;
    }

    @Override
    public Operator<Expression> le(final Object value) {
        setExpression(buildUnaryExpression("<=", value));
        return this;
    }

    @Override
    public Operator<Expression> not(final Object value) {
        setExpression(buildUnaryExpression("<>", value));
        return this;
    }

    @Override
    public Operator<Expression> between(final Object from, final Object to) {
        setExpression(buildBetweenExpression(from, to));
        return this;
    }

    @Override
    public Operator<Expression> like(final Object value) {
        Object expression = null;
        if (value != null) {
            expression = "%" + value + "%";
        }
        setExpression(buildUnaryExpression("LIKE", expression));
        return this;
    }

    @Override
    public Operator<Expression> in(final Object... values) {
        if (ArrayUtils.getLength(values) == 1) {
            eq(values[0]);
        } else if (ArrayUtils.isNotEmpty(values)) {
            final List<Object> cleanValues = Stream.of(values)
                    .filter(Objects::nonNull)
                    .toList();

            if (cleanValues.size() == 1) {
                eq(cleanValues.get(0));
            } else {
                Object expression = Stream.of(values)
                        .map(value -> value instanceof String ? String.format("'%s'", value) : String.valueOf(value))
                        .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                        .map(value -> "(" + value + ")")
                        .orElse(EMPTY);
                setExpression(buildUnaryExpression("IN", expression));
            }
        }
        return this;
    }

    @Override
    public String build() {
        String parentValue = EMPTY;
        if (this.parent != null) {
            parentValue = this.parent.build();
        }

        String expValue = EMPTY;
        if (StringUtils.isNotBlank(expression)) {
            expValue = expression;
        } else if (column != null) {
            expValue = column.build();
        }
        return String.join(SPACE, parentValue, expValue).trim();
    }

    @Override
    public String toString() {
        return expression;
    }

    private String buildUnaryExpression(final String operation, final Object value) {
        final String columnStr = this.column == null ? EMPTY : this.column.build();

        String expression;
        if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
            expression = String.format("%s IS NULL", columnStr);
        } else if (value instanceof String && !operation.equals("IN")) {
            expression = String.format("%s %s '%s'", columnStr, operation, value);
        } else {
            expression = String.format("%s %s %s", columnStr, operation, value);
        }
        return expression.trim();
    }

    private String buildBetweenExpression(final Object valueOne, final Object valueTwo) {
        final String columnStr = this.column == null ? EMPTY : this.column.build();

        String expression;
        if (valueOne == null || (valueOne instanceof String && StringUtils.isBlank((String) valueOne))) {
            expression = le(valueTwo).build();
        } else if (valueTwo == null || (valueTwo instanceof String && StringUtils.isBlank((String) valueTwo))) {
            expression = ge(valueOne).build();
        } else if ((valueOne instanceof String) || (valueTwo instanceof String)) {
            expression = String.format("%s BETWEEN '%s' AND '%s'", columnStr, valueOne, valueTwo);
        } else {
            expression = String.format("%s BETWEEN %s AND %s", columnStr, valueOne, valueTwo);
        }
        return expression.replaceFirst("\\(", EMPTY)
                .replaceFirst("\\)", EMPTY);
    }

    private static String appendParenthesis(final String expression) {
        if (expression.contains(" and ") || expression.contains(" or ")) {
            return String.format(EXP_TMPL, expression);
        }
        return expression;
    }

    @Override
    public int compareTo(Expression expression) {
        return Comparator.comparing(Expression::getExpression).compare(this, expression);
    }
}
