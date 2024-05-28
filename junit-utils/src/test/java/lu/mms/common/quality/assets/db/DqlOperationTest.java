package lu.mms.common.quality.assets.db;

import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.schema.Table;
import lu.mms.common.quality.assets.db.re.script.Dql;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class DqlOperationTest {

    @Test
    void shouldBuildDqlWhenIntersectOperation() {
        //Arrange
        final Column column = new Column(0, "ID", "NUMBER", true, true,
                false, 1);
        final Table table = new Table("CUSTOMER");
        table.setSchema(new Schema("PUBLIC"));

        // Act
        String result = Dql.select(List.of(column))
                .from(table)
                .intersect(
                        Dql.select(List.of(column)).from(table)
                ).build();

        // Assert
        assertThat(result, StringContains.containsString("INTERSECT"));
    }

    @Test
    void shouldBuildDqlWhenExceptOperation() {
        //Arrange
        final Column column = new Column(0, "ID", "NUMBER", true, true,
                false, 1);
        final Table table = new Table("CUSTOMER");
        table.setSchema(new Schema("PUBLIC"));

        // Act
        String result = Dql.select(List.of(column))
                .from(table)
                .except(
                        Dql.select(List.of(column)).from(table)
                ).build();

        // Assert
        assertThat(result, StringContains.containsString("EXCEPT"));
    }

    @Test
    void shouldBuildDqlWhenUnionOperation() {
        //Arrange
        final Column column = new Column(0, "ID", "NUMBER", true, true,
                false, 1);
        final Table table = new Table("CUSTOMER");
        table.setSchema(new Schema("PUBLIC"));

        // Act
        String result = Dql.select(List.of(column))
                .from(table)
                .union(
                        Dql.select(List.of(column)).from(table)
                ).build();

        // Assert
        assertThat(result, StringContains.containsString("UNION"));
    }
}
