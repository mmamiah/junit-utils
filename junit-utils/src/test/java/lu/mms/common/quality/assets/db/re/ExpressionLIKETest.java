package lu.mms.common.quality.assets.db.re;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionLIKETest {

    @Test
    void shouldBuildSqlExpressionINWhenNoValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object value = null;

        // Act
        Statement statement = Expression.value(columnName).like(value);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " IS NULL"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenStringValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object value = "str_value";

        // Act
        Statement statement = Expression.value(columnName).like(value);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " LIKE '%" + value + "%'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNumericValueProvided() {
        // Arrange
        final String columnName = "CASE_COLUMN_NAME";
        final Object value = RandomUtils.nextInt(0, 100);

        // Act
        Statement statement = Expression.value(columnName).like(value);

        // Assert
        assertThat(statement.toString(), equalTo(Expression.ALIAS + "." + columnName + " LIKE '%" + value + "%'"));
    }

}