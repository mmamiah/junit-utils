package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionBetweenTest {

    @Test
    void shouldBuildSqlExpressionINWhenNullValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = null;
        final Object two = null;

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " IS NULL"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNullLeftPropertyProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = null;
        final Object two = "two";

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " <= '" + two + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNullRightPropertyProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = "one";
        final Object two = null;

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " >= '" + one + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenStringValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final int one = 10;
        final int two = 20;

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " BETWEEN " + one + " AND " + two));
    }

    @Test
    void shouldBuildSqlExpressionINWhenMixStringNumericValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final String one = "10";
        final int two = 20;

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " BETWEEN '" + one + "' AND '" + two + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenMixNumericStringValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final int one = 10;
        final String two = "20";

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " BETWEEN '" + one + "' AND '" + two + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNumericValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final int one = 10;
        final int two = 20;

        // Act
        Statement statement = Expression.property(column).between(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " BETWEEN " + one + " AND " + two));
    }

}