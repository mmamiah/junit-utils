package lu.mms.common.quality.userguide.mybatis;

import lu.mms.common.quality.assets.mybatis.InjectMapper;
import lu.mms.common.quality.assets.mybatis.MyBatisMapperTest;
import lu.mms.common.quality.userguide.dao.CustomerMapper;
import lu.mms.common.quality.userguide.dao.NonMapperBean;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@MyBatisMapperTest(
    testIsolation = false,
    script = {"sql/oracle/schema.sql", "sql/oracle/data.sql"}
)
@TestMethodOrder(OrderAnnotation.class)
class MyBatisMapperTestExample1Test {

    private static final String NEW_CUSTOMER_NAME = "new_customer";

    @InjectMapper
    private CustomerMapper sut;

    @InjectMapper
    private NonMapperBean nonMapperBean;

    @Test
    @Order(1)
    void shouldInitMyBatisMapperWhenClassConfig(final JdbcTemplate jdbcTemplate) {
        // Arrange
        final int firstIdInDB = 1; // ID in 'data.sql'

        // Act
        final String name = sut.findCustomerNameById(firstIdInDB);

        // Assert
        assertThat(name, equalTo("alpha"));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @Order(2)
    void shouldInsertCustomerInGlobalSessionWhenNoNewConfigDefined(final JdbcTemplate jdbcTemplate) {
        // Arrange
        final int newId = IntStream.of(0, 100)
                .filter(index -> sut.findCustomerNameById(index) == null)
                .findFirst()
                .orElse(0);

        // Act
        final int count = sut.insertCustomer(newId, NEW_CUSTOMER_NAME);

        // Assert
        assertThat(count, equalTo(1));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER", Integer.class), 0);
        assertThat(itemCount, equalTo(3));
    }

    @Test
    @Order(3)
    void shouldUseGlobalConfigWhenNoConfigAtMethodLevel(final JdbcTemplate jdbcTemplate) {
        // Arrange
        final int firstIdInDB = 1; // ID in 'data.sql'

        // Act
        final String name = sut.findCustomerNameById(firstIdInDB);

        // Assert
        assertThat(name, equalTo("alpha"));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @Order(4)
    // tag::method_level_example[]
    @MyBatisMapperTest(script = "sql/oracle/schema.sql")
    void shouldUseSpecificConfigConfigWhenConfigAtMethodLevel(final JdbcTemplate jdbcTemplate) {
        // Arrange
        final int firstIdInDB = 1; // ID in 'data.sql'

        // Act
        final String name = sut.findCustomerNameById(firstIdInDB);

        // Assert
        assertThat(name, nullValue());

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER", Integer.class), 0);
        assertThat(itemCount, equalTo(0));
    }
    // end::method_level_example[]

    @Test
    @Order(5)
    void shouldConfirmGlobalSessionStillAliveWhenNoMethodConfigDefined(final JdbcTemplate jdbcTemplate) {
        // Act
        final int idOne = 1; // ID in 'data.sql'
        final int idTwo = 2; // ID in 'data.sql'
        final String customerOne = sut.findCustomerNameById(idOne);
        final String customerTwo = sut.findCustomerNameById(idTwo);

        // Assert
        assertThat(customerOne, equalTo("alpha"));
        assertThat(customerTwo, equalTo("beta"));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = ObjectUtils.defaultIfNull(jdbcTemplate.queryForObject("select count(*) from CUSTOMER where ID > 0", Integer.class), 0);
        assertThat(itemCount, equalTo(2));
    }

}
