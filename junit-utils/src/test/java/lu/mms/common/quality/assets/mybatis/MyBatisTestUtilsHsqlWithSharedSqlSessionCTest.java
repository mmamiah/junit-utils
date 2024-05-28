package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.db.InMemoryDb;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * This test check that SQL session is available and shared (<i>testIsolation = false</i>) for all test cases. <BR>
 * The test case
 */
@MyBatisMapperTest(
        dbEngine = InMemoryDb.HSQL_ORACLE,
        script = {
            "sql/schema.sql",
            "sql/hsql/sequence.sql",
            "sql/hsql/trigger.sql",
            "sql/hsql/stored_procedure.sql",
            "sql/data-for-test_class.sql"
        },
        testIsolation = false
)
class MyBatisTestUtilsHsqlWithSharedSqlSessionCTest {

    @InjectMapper
    private EntityMapper sut;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resolveJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 9})
    void shouldInitSharedSqlSessionWhenValidConfiguration(final Integer id) {
        // Arrange
        final String customerNotExist = sut.findCustomerNameById(0);
        assumeTrue(StringUtils.isBlank(customerNotExist));

        // Act
        final String customer = sut.findCustomerNameById(id);

        // Assert
        assertThat(customer, notNullValue());

        final int countItems = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countItems, greaterThanOrEqualTo(4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"paul", "pit", "pierre", "pascal"})
    void shouldInitSharedSqlSessionWhenAddingNewRecordsToDatasource(final String name) {
        // Arrange
        int id;
        do {
            // retrieve an available ID
            id = RandomUtils.nextInt(0, 100);
        } while(StringUtils.isNotBlank(sut.findCustomerNameById(id)));

        // Act
        final boolean inserted = sut.insertCustomer(id, name);

        // Assert
        assertThat(inserted, equalTo(true));
        final String customer = sut.findCustomerNameById(id);
        assertThat(customer, notNullValue());

        // Assert state didn't change as per test (session) isolation
        final int countItems = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countItems, greaterThanOrEqualTo(5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"paul", "pit", "pierre", "pascal"})
    @MyBatisMapperTest(
            dbEngine = InMemoryDb.HSQL_ORACLE,
            script = {
                    "sql/schema.sql",
                    "sql/hsql/sequence.sql",
                    "sql/hsql/trigger.sql",
                    "sql/hsql/stored_procedure.sql",
                    "sql/data-for-test_class.sql"
            }
    )
    void shouldInitIsolatedSqlSessionWhenMapperConfigAtMethodLevel(final String name) {
        // Arrange
        final int id = RandomUtils.nextInt(50, 100);
        final String customerNotExist = sut.findCustomerNameById(id);
        assumeTrue(StringUtils.isBlank(customerNotExist), "The generated ID [" + id + "] already exists.");

        // Act
        final boolean inserted = sut.insertCustomer(id, name);

        // Assert
        assertThat(inserted, equalTo(true));
        final String customer = sut.findCustomerNameById(id);
        assertThat(customer, notNullValue());

        // Assert state didn't change as per test (session) isolation
        final int countItems = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countItems, equalTo(6));
    }

    @Test
    @MyBatisMapperTest(script = {
            "sql/schema.sql",
            "sql/hsql/sequence.sql",
            "sql/hsql/trigger.sql",
            "sql/hsql/stored_procedure.sql",
            "sql/data-for-test_method.sql"
    })
    void shouldInitMapperAtTestMethodLevel() {
        // Arrange

        // Act
        final String customer = sut.findCustomerNameById(3);

        // Assert
        assertThat(customer, containsString("method"));

        final int countItems = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countItems, IsEqual.equalTo(3));
    }

    @Test
    @MyBatisMapperTest(script = {
            "sql/schema.sql",
            "sql/hsql/sequence.sql",
            "sql/hsql/trigger.sql",
            "sql/hsql/stored_procedure.sql",
            "sql/data-for-test_method.sql"
    })
    void shouldGenerateNewIdFromTriggerWhenInsertingNewEntity() {
        // Arrange
        final int countBeforeInsert = ObjectUtils.defaultIfNull(
                jdbcTemplate.queryForObject("select count(*) from customer where ID > 0", Integer.class),
                0
        );
        final int newId = countBeforeInsert + 1;
        final String name = RandomStringUtils.randomAlphanumeric(5);

        // Act
        final boolean isInserted = sut.insertCustomer(newId, name);

        // Assert
        assertThat(isInserted, CoreMatchers.equalTo(true));

        final String triggerValue = sut.findCustomerTriggerCountById(newId);
        assertThat(triggerValue, notNullValue());

        final int countAfterInsert = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countAfterInsert, greaterThan(countBeforeInsert));
    }

    @Test
    @MyBatisMapperTest(script = {
            "sql/schema.sql",
            "sql/hsql/sequence.sql",
            "sql/hsql/trigger.sql",
            "sql/hsql/stored_procedure.sql",
            "sql/data-for-test_method.sql"
    })
    void shouldCallTheStoredProcedureWhenUpdatingTheEntity() {
        // Arrange

        // Act
        sut.updateTimeStampViaStoredProcedure(3);

        // Assert
        final Date date = sut.findCustomerUpdateTimeById(3);
        assertThat(date, notNullValue());
    }

    /**
     * Testcase to check that the use of {@link MyBatisMapperTest} at method level is using the specified context.
     * The datasource schema is initialized without any data.
     */
    @Test
    @MyBatisMapperTest(script = "sql/schema.sql")
    void shouldUseSpecificConfigWhenConfigAtMethodLevel() {
        // Arrange
        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from customer", Integer.class), 0);
        assumeTrue(itemCount == 0);
        final Integer searchId = RandomUtils.nextInt(0, 1000);

        // Act
        final String name = sut.findCustomerNameById(searchId);

        // Assert
        assertThat(name, nullValue());
    }

    /**
     * Testcase to check that the use of {@link MyBatisMapperTest} at method level is using the specified context.
     * The datasource schema is initialized without any data.
     */
    @Test
    @MyBatisMapperTest(script = "sql/schema.sql", testIsolation = false)
    void shouldNotConsiderTestIsolationAtFalseForMethodConfig() {
        // Arrange
        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER", Integer.class), 0);
        assumeTrue(itemCount == 0);
        final Integer searchId = RandomUtils.nextInt(0, 1000);

        // Act
        final String name = sut.findCustomerNameById(searchId);

        // Assert
        assertThat(name, nullValue());
    }

}
