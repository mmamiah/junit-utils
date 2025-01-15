package lu.mms.common.quality.assets.db;

import lu.mms.common.quality.assets.db.re.Expression;
import lu.mms.common.quality.assets.db.re.Function;
import lu.mms.common.quality.assets.db.re.schema.Column;
import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.schema.Table;
import lu.mms.common.quality.assets.db.re.script.Dql;
import lu.mms.common.quality.assets.db.re.script.OrderBy;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class DqlOperationTest {

    @Test
    void shouldBuildDqlWhenIntersectOperation() {
        //Arrange
        final Table table = new Table("CUSTOMER");
        table.setSchema(new Schema("PUBLIC"));
        final Column column = table.addColumn(new Column("ID"));

        // Act
        String result = Dql.select(column)
                .from(table)
                .intersect(
                        Dql.select(column).from(table)
                ).build();

        // Assert
        assertThat(result, StringContains.containsString("INTERSECT"));
    }

    @Test
    void shouldBuildDqlWhenExceptOperation() {
        //Arrange
        final Table table = new Table("CUSTOMER");
        table.setSchema(new Schema("PUBLIC"));
        final Column column = table.addColumn(new Column("ID"));

        // Act
        String result = Dql.select(column)
                .from(table)
                .except(
                        Dql.select(column).from(table)
                ).build();

        // Assert
        assertThat(result, StringContains.containsString("EXCEPT"));
    }

    @Test
    void shouldBuildDqlWhenUnionOperation() {
        //Arrange
        final Table table = new Table("CUSTOMER");
        table.setSchema(new Schema("PUBLIC"));
        final Column column = table.addColumn(new Column("ID"));

        // Act
        String result = Dql.select(column)
                .from(table)
                .union(
                        Dql.select(column).from(table)
                ).build();

        // Assert
        assertThat(result, StringContains.containsString("UNION"));
    }

    @Test
    void useCaseOne() {
        //Arrange
        final  Schema schemaPublic = new Schema("PUBLIC");

        final Table tableCustomer = new Table("CUSTOMER");
        final Column customerId = tableCustomer.addColumn(new Column(0, "ID", "NUMBER", true, true, false, 1));
        final Column cModel = tableCustomer.addColumn(new Column(1, "MODEL", "STRING"));
        final Column cType = tableCustomer.addColumn(new Column(1, "TYPE", "STRING"));
        final Column cPower = tableCustomer.addColumn(new Column(1, "POWER", "STRING"));
        final Column cName = tableCustomer.addColumn(new Column(1, "NAME", "STRING"));
        final Column cAddress = tableCustomer.addColumn(new Column(1, "ADDRESS", "STRING"));
        schemaPublic.addTable(tableCustomer);

        final Table tableCar = new Table("CAR");
        final Column ownerId = tableCar.addColumn(new Column(1, "ID_OWNER", "NUMBER"));
        final Column model = tableCar.addColumn(new Column(1, "MODEL", "STRING"));
        final Column type = tableCar.addColumn(new Column(1, "TYPE", "STRING"));
        final Column power = tableCar.addColumn(new Column(1, "POWER", "STRING"));
        final Column productionYear = tableCar.addColumn(new Column(1, "PRODUCTION_YEAR", "STRING"));
        schemaPublic.addTable(tableCar);

        // Act
        final String sql = Dql.select(Function.fn().count(customerId)) //, customerId, carId, cModel
                .from(tableCustomer)
                .fullJoin("ID", ownerId)
                .leftJoin("MODEL", model)
                .rightJoin("TYPE", type)
                .join("POWER", power)
                .where(Expression.property(cName).eq("MIKE").or(Expression.property(cAddress).ge(null)))
                .groupBy(cModel, cType)
                .having(Function.fn().count(cType).gt(5).or(Expression.property(cType).eq(1)))
                .orderBy(productionYear, OrderBy.OrderByType.DESC)
                .orderBy(cPower, OrderBy.OrderByType.ASC)
                .build();

        // Assert
        assertThat(sql, StringContains.containsString("SELECT"));
        assertThat(sql, StringContains.containsString("FROM"));
        assertThat(sql, StringContains.containsString("JOIN"));
        assertThat(sql, StringContains.containsString("WHERE"));
        assertThat(sql, StringContains.containsString("GROUP BY"));
        assertThat(sql, StringContains.containsString("HAVING"));
        assertThat(sql, StringContains.containsString("ORDER BY"));
    }

    @Test
    void useCaseTwo() {
        //Arrange
        final  Schema schemaPublic = new Schema("PUBLIC");

        final Table tableCustomer = new Table("CUSTOMER");
        final Column columnCustomerId = new Column(0, "ID", "NUMBER", true, true, false, 1);
        schemaPublic.addTable(tableCustomer);
        tableCustomer.addColumn(columnCustomerId);
        final Column cName = tableCustomer.addColumn(new Column(1, "NAME", "STRING"));

        final Table tableCar = new Table("CAR");
        final Column cardId = new Column(0, "ID", "NUMBER", true, true, false, 1);
        final Column ownerId = new Column(1, "ID_OWNER", "NUMBER");
        tableCar.addColumn(cardId);
        tableCar.addColumn(ownerId);
        schemaPublic.addTable(tableCar);

        // Act
        final String sql = Dql.select(ownerId)
                .from(tableCustomer)
                .where(Expression.property(cName).eq("MIKE"))
                .groupBy(ownerId)
                .orderBy(ownerId)
                .build();

        // Assert
        assertThat(sql, StringContains.containsString("SELECT"));
        assertThat(sql, StringContains.containsString("FROM"));
        assertThat(sql, StringContains.containsString("WHERE"));
        assertThat(sql, StringContains.containsString("GROUP BY"));
        assertThat(sql, StringContains.containsString("ORDER BY"));
    }

    @Test
    void useCaseThree() {
        //Arrange
        final  Schema schemaPublic = new Schema("PUBLIC");

        final Table tableCustomer = new Table("CUSTOMER");
        final Column columnCustomerId = new Column(0, "ID", "NUMBER", true, true, false, 1);
        schemaPublic.addTable(tableCustomer);
        tableCustomer.addColumn(columnCustomerId);
        final Column cName = tableCustomer.addColumn(new Column(1, "NAME", "STRING"));

        final Table tableCar = new Table("CAR");
        final Column cardId = new Column(0, "ID", "NUMBER", true, true, false, 1);
        final Column ownerId = new Column(1, "ID_OWNER", "NUMBER");
        tableCar.addColumn(cardId);
        tableCar.addColumn(ownerId);
        schemaPublic.addTable(tableCar);

        // Act
        final String sql = Dql.select(cardId)
                .from(tableCustomer)
                .where(Expression.property(cName).eq("MIKE"))
                .orderBy(cardId)
                .build();

        // Assert
        assertThat(sql, StringContains.containsString("SELECT"));
        assertThat(sql, StringContains.containsString("FROM"));
        assertThat(sql, StringContains.containsString("WHERE"));
        assertThat(sql, StringContains.containsString("ORDER BY"));
    }

    @Test
    void useCaseFour() {
        //Arrange
        final  Schema schemaPublic = new Schema("PUBLIC");

        final Table tableCustomer = new Table("CUSTOMER");
        final Column columnCustomerId = new Column(0, "ID", "NUMBER", true, true, false, 1);
        schemaPublic.addTable(tableCustomer);
        tableCustomer.addColumn(columnCustomerId);

        final Table tableCar = new Table("CAR");
        final Column cardId = new Column(0, "ID", "NUMBER", true, true, false, 1);
        final Column ownerId = new Column(1, "ID_OWNER", "NUMBER");
        tableCar.addColumn(cardId);
        tableCar.addColumn(ownerId);
        schemaPublic.addTable(tableCar);

        // Act
        final String sql = Dql.select(cardId)
                .from(tableCustomer)
                .orderBy(columnCustomerId)
                .build();

        // Assert
        assertThat(sql, StringContains.containsString("SELECT"));
        assertThat(sql, StringContains.containsString("FROM"));
        assertThat(sql, StringContains.containsString("ORDER BY"));
    }

    @Test
    void useCaseFive() {
        final Table tableCar = new Table("CAR");
        final Column columnCustomerId = new Column(0, "ID_CUSTOMER", "NUMBER");
        final Column cardId = new Column(0, "ID_CAR", "NUMBER");
        tableCar.addColumn(cardId);
        tableCar.addColumn(columnCustomerId);
        final String value = Function.fn().concat(cardId, columnCustomerId).build();

        assertThat(value, StringContains.containsString("CONCAT"));
    }

    @Test
    void useCaseSix() {
        final Table tableCar = new Table("CAR");
        final Column columnCustomerId = new Column(0, "ID_CUSTOMER", "NUMBER");
        final Column cardId = new Column(0, "ID_CAR", "NUMBER");
        tableCar.addColumn(cardId);
        tableCar.addColumn(columnCustomerId);
        final String value = Function.fn().avg(cardId).build();

        assertThat(value, StringContains.containsString("AVG"));
    }
}
