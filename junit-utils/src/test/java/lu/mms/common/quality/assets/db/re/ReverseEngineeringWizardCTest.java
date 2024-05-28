package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.InMemoryDb;
import lu.mms.common.quality.assets.db.re.schema.Record;
import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.schema.Table;
import lu.mms.common.quality.assets.db.re.script.Relation;
import lu.mms.common.quality.assets.mybatis.MyBatisMapperTest;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;


/**
 * This test check that SQL session is available for all test cases. <BR>
 * The test case
 */
@MyBatisMapperTest(
        dbEngine = InMemoryDb.H2_ORACLE,
        script = {
        "sql/schema.sql",
        "sql/data.sql"}
)
class ReverseEngineeringWizardCTest {

    private ReverseEngineeringWizard sut;

    @BeforeEach
    void resolveSqlSessionFactory(final SqlSessionFactory sqlSessionFactory) {
        final DataSource dataSource = sqlSessionFactory.openSession().getConfiguration().getEnvironment().getDataSource();
        sut = new ReverseEngineeringWizard(dataSource, "PUBLIC");
    }

    @Test
    void shouldExtractAllTableEntriesWhenTableNameOnly() {
        // Arrange
        sut.withTable("DEVICE");

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());
        assertThat(schema.getName(), equalTo("PUBLIC"));
        assertThat(schema.getTables().size(), equalTo(1));

        // Assert schema values
        assertThat(schema.getColumnValues("DEVICE", "ID"),
                containsInAnyOrder(BigDecimal.ONE, new BigDecimal(2), new BigDecimal(3), new BigDecimal(4)));
        assertThat(schema.getColumnValues("DEVICE", "LA_NAME"),
                containsInAnyOrder("'mobile phone'", "'TV'", "'Bicycle'", "'Table'"));
        assertThat(schema.getColumnValues("DEVICE", "LA_DESCRIPTION"),
                containsInAnyOrder("'A handy'", "'Television'", "'Like a motor ... but a light'", "'just a simple table.'"));

        // Assert records
        final Table table = schema.getTables().values().stream().findFirst().orElseThrow();
        assertThat(table.getRecords().size(), equalTo(4));
    }

    @Test
    void shouldExtractAnyTableEntryAndRelationWhenPkAndFk() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").eq(1));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());
        assertThat(schema.getName(), equalTo("PUBLIC"));
        assertThat(schema.getTables().size(), equalTo(1));

        final Table address = schema.getTables().get("ADDRESS");
        assertThat(address.getRecords().size(), equalTo(1));
    }

    @Test
    void shouldExtractFullRecordValuesWhenExists() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").eq(1));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final Record address = schema.getTables().get("ADDRESS").getRecords().stream().findFirst().orElseThrow();
        assertThat(address.getValue("ID"), equalTo(BigDecimal.ONE));
        assertThat(address.getValue("NU_NUMBER"), equalTo(new BigDecimal(42)));
        assertThat(address.getValue("LA_STREET"), equalTo("'Rte de Luxembourg'"));
        assertThat(address.getValue("LA_POSTAL_CODE"), equalTo("'4590'"));
        assertThat(address.getValue("LA_CITY"), equalTo("'Bascharage'"));
        assertThat(address.getValue("LA_COUNTRY"), equalTo("'Luxembourg'"));
        assertThat(address.getValue("TS_UPDATE"), nullValue());
    }

    @Test
    void shouldExtractRecordsValuesWhenComparisonIN() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").in(1, 3, 90));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(1));
    }

    @Test
    void shouldExtractRecordsValuesWhenComparisonGE() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").ge('5'));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(2));
    }

    @Test
    void shouldExtractRecordsValuesWhenComparisonLE() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").le('2'));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(3));
    }

    @Test
    void shouldExtractRecordsValuesWhenComparisonNOT() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").not('2'));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(4));
    }

    @Test
    void shouldExtractRecordsValuesWhenComparisonLIKE() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("LA_STREET").like("Rte"));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(2));
    }

    @Test
    void shouldExtractRecordsValuesWhenComparisonBETWEEN() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").between(4, 9));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(2));
    }

    @Test
    void shouldExtractRecordsValuesWhenInnerJoin() {
        // Arrange
        sut.withLevel(1)
                .withTable("ADDRESS", Relation.join("ID", "CUSTOMER_ADDRESS", "ID_ADDRESS"))
                .withTable("ADDRESS", Expression.value("ID").ge(6))
                .withTable("CUSTOMER", Expression.value("ID").between(7, 9));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(1));

        // Customer values
        final List<Record> customers = schema.getTables().get("CUSTOMER").getRecords();
        assertThat(customers.size(), equalTo(1));

        // CUSTOMER_ADDRESS values
        final List<Record> customerAddresses = schema.getTables().get("CUSTOMER_ADDRESS").getRecords();
        assertThat(customerAddresses.size(), equalTo(0));
    }

    @Test
    void shouldExtractRecordsValuesWhenScanLevelIsZero() {
        // Arrange
        sut.withLevel(1).withTable("CUSTOMER", Expression.value("ID").between(7, 9));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        assertThat(schema.getTables().get("ADDRESS"), nullValue());

        // Customer values
        final List<Record> customers = schema.getTables().get("CUSTOMER").getRecords();
        assertThat(customers.size(), equalTo(1));

        // CUSTOMER_ADDRESS values
        final List<Record> customerAddresses = schema.getTables().get("CUSTOMER_ADDRESS").getRecords();
        assertThat(customerAddresses.size(), equalTo(1));
    }

    @Test
    void shouldExtractRecordsValuesWhenScanLevelIsDefined() {
        // Arrange
        sut.withLevel(2)
                .withTable("CUSTOMER", Expression.value("ID").between(7, 9));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(1));

        // Customer values
        final List<Record> customers = schema.getTables().get("CUSTOMER").getRecords();
        assertThat(customers.size(), equalTo(1));

        // CUSTOMER_ADDRESS values
        final List<Record> customerAddresses = schema.getTables().get("CUSTOMER_ADDRESS").getRecords();
        assertThat(customerAddresses.size(), equalTo(1));
    }

    @Test
    void shouldExtractRecordsValuesWhenConjunctionAND() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").between(2, 5).and(Expression.value("NU_NUMBER").between(30, 40)));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(1));

        assertThat(addresses.get(0).getValue("ID").toString(), equalTo("2"));
    }

    @Test
    void shouldExtractRecordsValuesWhenConjunctionOR() {
        // Arrange
        sut.withTable("ADDRESS", Expression.value("ID").eq(3).or(Expression.value("ID").eq(5)));

        // Act
        final Schema schema = sut.build();

        // Assert
        assertThat(schema, notNullValue());

        // Address values
        final List<Record> addresses = schema.getTables().get("ADDRESS").getRecords();
        assertThat(addresses.size(), equalTo(1));

        assertThat(addresses.get(0).getValue("ID").toString(), equalTo("5"));
    }

}
