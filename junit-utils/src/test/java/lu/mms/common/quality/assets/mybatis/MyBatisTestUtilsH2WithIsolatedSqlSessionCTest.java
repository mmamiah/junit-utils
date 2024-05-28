package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.db.InMemoryDb;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * This test check that SQL session is available for all test cases. <BR>
 * The test case
 */
@MyBatisMapperTest(
        dbEngine = InMemoryDb.H2_ORACLE,
        script = {
        "sql/schema.sql",
        "sql/data-for-test_class.sql"}
)
class MyBatisTestUtilsH2WithIsolatedSqlSessionCTest {

    @InjectMapper
    private EntityMapper sut;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resolveJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 9})
    void shouldInitSessionFactoryNrOneWhenValidConfiguration(final Integer id) {
        // Arrange
        final String customerNotExist = sut.findCustomerNameById(0);
        assumeTrue(StringUtils.isBlank(customerNotExist));

        // Act
        final String customer = sut.findCustomerNameById(id);

        // Assert
        assertThat(customer, notNullValue());

        final int countItems = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(countItems, equalTo(5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"paul", "pit", "pierre", "pascal"})
    void shouldInitIsolatedSqlSessionWhenDefaultIsolation(final String name) {
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

    @ParameterizedTest
    @ValueSource(strings = {"paul", "pit", "pierre", "pascal"})
    @MyBatisMapperTest(
            dbEngine = InMemoryDb.H2_ORACLE,
            script = {
                    "sql/schema.sql",
                    "sql/data-for-test_class.sql"}
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

}
