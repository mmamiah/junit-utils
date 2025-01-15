package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Table;
import org.junit.jupiter.api.Test;

import static org.apache.commons.rng.simple.RandomSource.XO_RO_SHI_RO_128_PP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionGTTest {

    @Test
    void shouldBuildSqlExpressionINWhenNoPropertyProvided() {
        // Arrange
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object value = null;

        // Act
        Statement statement = Expression.property(column).gt(value);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " IS NULL"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenStringPropertyProvided() {
        // Arrange
        
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object value = "str_value";

        // Act
        Statement statement = Expression.property(column).gt(value);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " > '" + value + "'"));
    }

    @Test
    void shouldBuildSqlExpressionINWhenNumericPropertyProvided() {
        // Arrange
        
        final Table tableCustomer = new Table("CASE_TABLE");
        final Column column = tableCustomer.addColumn(new Column("CASE_COLUMN_NAME"));
        final Object value = XO_RO_SHI_RO_128_PP.create().nextInt(0, 100);

        // Act
        Statement statement = Expression.property(column).gt(value);

        // Assert
        assertThat(statement.toString(), equalTo(column.build() + " > " + value));
    }

}