package lu.mms.common.quality.assets.db.re;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The SQL Expression.
 */
public class Expression implements Comparable<Expression>, Operator<Expression>, Condition<Expression> {

    public static final String ALIAS = "#alias_placeholder#";
    private static final String EXP_TMPL = "(%s)";

    private final String columnName;
    private String expression = StringUtils.EMPTY;
    private String alias;

    private Expression(final String columnName) {
        this.columnName = columnName;
    }

    public static Expression value(final String columnName) {
        return new Expression(columnName);
    }

    String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = StringUtils.isBlank(expression) ? StringUtils.EMPTY : expression;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public void applyAlias(final String alias) {
        this.alias = alias;
        expression = expression.replace(ALIAS, alias);
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
        setExpression(buildUnaryExpression( "LIKE", expression));
        return this;
    }

    @Override
    public Operator<Expression> in(final Object... values) {
        if (ArrayUtils.getLength(values) == 1) {
            eq(values[0]);
        } else if (ArrayUtils.isNotEmpty(values)) {
            final List<Object> cleanValues = Stream.of(values)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (cleanValues.size() == 1) {
                eq(cleanValues.get(0));
            } else {
                Object expression = Stream.of(values)
                        .map(value -> value instanceof String ? String.format("'%s'", value) : String.valueOf(value))
                        .reduce((a, b) -> StringUtils.joinWith(", ", a, b))
                        .map(value -> "(" + value + ")")
                        .orElse(StringUtils.EMPTY);
                setExpression(buildUnaryExpression("IN", expression));
            }
        }
        return this;
    }

    @Override
    public String build() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }

    private String buildUnaryExpression(final String operation, final Object value) {
        String expression;
        if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
            expression = String.format(ALIAS + ".%s IS NULL", columnName);
        } else if (value instanceof String && !operation.equals("IN")) {
            expression = String.format(ALIAS + ".%s %s '%s'", columnName, operation, value);
        } else {
            expression = String.format(ALIAS + ".%s %s %s", columnName, operation, value);
        }
        return expression;
    }

    private String buildBetweenExpression(final Object valueOne, final Object valueTwo) {
        String expression;
        if (valueOne == null || (valueOne instanceof String && StringUtils.isBlank((String) valueOne))) {
            expression = le(valueTwo).build();
        } else if (valueTwo == null || (valueTwo instanceof String && StringUtils.isBlank((String) valueTwo))) {
            expression = ge(valueOne).build();
        } else if ((valueOne instanceof String) || (valueTwo instanceof String)) {
            expression = String.format(ALIAS + ".%s BETWEEN '%s' AND '%s'", columnName, valueOne, valueTwo);
        } else {
            expression = String.format(ALIAS + ".%s BETWEEN %s AND %s", columnName, valueOne, valueTwo);
        }
        return expression.replaceFirst("\\(", StringUtils.EMPTY)
                .replaceFirst("\\)", StringUtils.EMPTY);
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
