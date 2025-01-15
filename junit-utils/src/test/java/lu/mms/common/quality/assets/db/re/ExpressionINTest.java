package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionINTest {

    @Test
    void shouldBuildSqlExpressionINWhenNoPropertyProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object value = null;

        // Act
        Statement statement = Expression.property(column).in(value);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " IS NULL"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenSingleStringPropertyProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object value = "str_value";

        // Act
        Statement statement = Expression.property(column).in(value);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " = '" + value + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenStringValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = "v1";
        final Object two = "v2";

        // Act
        Statement statement = Expression.property(column).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " IN ('" + one + "', '" + two + "')"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenMixValuesProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = 123;
        final Object two = "v2";

        // Act
        Statement statement = Expression.property(column).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " IN (" + one + ", '" + two + "')"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenPropertyAndNullProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = "v1";
        final Object two = null;

        // Act
        Statement statement = Expression.property(column).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " = '" + one + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNullAndPropertyAndProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object one = null;
        final Object two = "v2";

        // Act
        Statement statement = Expression.property(column).in(one, two);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " = '" + two + "'"));
    }

}