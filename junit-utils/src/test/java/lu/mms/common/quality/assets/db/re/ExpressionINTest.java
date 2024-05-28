package lu.mms.common.quality.assets.db.re;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionINTest {

    @Test
    void shouldBuildSqlExpressionINWhenNoValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object value = null;

        // Act
        Statement statement = Expression.value(columnName).in(value);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " IS NULL"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenSingleStringValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object value = "str_value";

        // Act
        Statement statement = Expression.value(columnName).in(value);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " = '" + value + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenStringValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = "v1";
        final Object two = "v2";

        // Act
        Statement statement = Expression.value(columnName).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " IN ('" + one + "', '" + two + "')"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenMixValuesProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = 123;
        final Object two = "v2";

        // Act
        Statement statement = Expression.value(columnName).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " IN (" + one + ", '" + two + "')"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenValueAndNullProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = "v1";
        final Object two = null;

        // Act
        Statement statement = Expression.value(columnName).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " = '" + one + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNullAndValueAndProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object one = null;
        final Object two = "v2";

        // Act
        Statement statement = Expression.value(columnName).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " = '" + two + "'"));
    }

}