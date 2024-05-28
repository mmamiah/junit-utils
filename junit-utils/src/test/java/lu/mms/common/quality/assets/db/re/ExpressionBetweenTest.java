package lu.mms.common.quality.assets.db.re;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionBetweenTest {

    @Test
    void shouldBuildSqlExpressionINWhenNullValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = null;
        final Object two = null;

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " IS NULL"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNullLeftValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = null;
        final Object two = "two";

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " <= '" + two + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNullRightValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = "one";
        final Object two = null;

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " >= '" + one + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenStringValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final int one = 10;
        final int two = 20;

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " BETWEEN " + one + " AND " + two));
    }

    @Test
    void shouldBuildSqlExpressionINWhenMixStringNumericValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final String one = "10";
        final int two = 20;

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " BETWEEN '" + one + "' AND '" + two + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenMixNumericStringValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final int one = 10;
        final String two = "20";

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " BETWEEN '" + one + "' AND '" + two + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNumericValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final int one = 10;
        final int two = 20;

        // Act
        Statement statement = Expression.value(columnName).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " BETWEEN " + one + " AND " + two));
    }

}