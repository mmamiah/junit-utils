package lu.mms.common.quality.assets.mybatis;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testing the {@link MyBatisMapperTest} annotation with default config (HSQL).
 */
@MyBatisMapperTest(script = {
        "sql/schema.sql",
        "sql/hsql/sequence.sql",
        "sql/hsql/trigger.sql",
        "sql/hsql/stored_procedure.sql",
        "sql/data-for-test_class.sql"
})
class MyBatisTestUtilsHsqlCTest {

    @InjectMapper
    private EntityMapper sut;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resolveJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void shouldFindValuesFromDefaultConfig() {
        // Act
        final String name = sut.findCustomerNameById(1);

        // Assert
        assertThat(name, equalTo("alpha"));

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(itemCount, equalTo(5));
    }

    @Test
    @MyBatisMapperTest(script = {
            "sql/schema.sql",
            "sql/data-for-test_method.sql"
    })
    void shouldInitMapperAtTestMethodLevel() {
        // Arrange

        // Act
        final String customer = sut.findCustomerNameById(3);

        // Assert
        assertThat(customer, containsString("method"));

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(itemCount, IsEqual.equalTo(3));
    }

    @Test
    @MyBatisMapperTest(script = {
            "sql/schema.sql",
            "sql/hsql/sequence.sql",
            "sql/hsql/trigger.sql",
            "sql/data-for-test_method.sql"
    })
    void shouldGenerateNewIdFromTriggerWhenInsertingNewEntity() {
        // Arrange
        final int countBeforeInsert = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        final int newId = countBeforeInsert + 1;
        final String name = RandomStringUtils.randomAlphanumeric(5);

        // Act
        final boolean isInserted = sut.insertCustomer(newId, name);

        // Assert
        assertThat(isInserted, equalTo(true));

        final String triggerValue = sut.findCustomerTriggerCountById(newId);
        assertThat(triggerValue, notNullValue());

        final int countAfterInsert = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countAfterInsert, greaterThan(countBeforeInsert));
    }

    @Test
    @MyBatisMapperTest(script = {
            "sql/schema.sql",
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
        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER", Integer.class), 0);
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
