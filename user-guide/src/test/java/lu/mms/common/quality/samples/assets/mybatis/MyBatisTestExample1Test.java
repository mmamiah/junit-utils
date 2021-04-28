package lu.mms.common.quality.samples.assets.mybatis;

import lu.mms.common.quality.assets.mybatis.InjectMapper;
import lu.mms.common.quality.assets.mybatis.MyBatisTest;
import lu.mms.common.quality.assets.mybatis.MyBatisTestUtils;
import lu.mms.common.quality.samples.dao.CustomerMapper;
import lu.mms.common.quality.samples.dao.NonMapperBean;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@MyBatisTest(
    testIsolation = false,
    config = "mybatis-test-config.xml",
    script = {"schema-alpha.sql", "data-beta.sql"}
)
@TestMethodOrder(OrderAnnotation.class)
class MyBatisTestExample1Test {

    private static final String NEW_CUSTOMER_NAME = "new_customer";
    private static final Integer NEW_CUSTOMER_ID = 3;
    private static final String CUSTOMER = "customer";
    private static final String ID_GREATHER_THAN_0 = "ID > 0";

    @InjectMapper
    private CustomerMapper sut;

    @InjectMapper
    private NonMapperBean nonMapperBean;

    @Test
    @Order(1)
    void shouldInitMyBatisMapperWhenClassConfig() {
        // Act
        final String name = sut.findCustomerNameById(1);

        // Assert
        assertThat(name, equalTo("alpha"));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @Order(2)
    void shouldInsertCustomerInGlobalSessionWhenNoNewConfigDefined() {
        // Act
        final int count = sut.insertCustomer(NEW_CUSTOMER_ID, NEW_CUSTOMER_NAME);

        // Assert
        assertThat(count, equalTo(1));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(3));
    }

    @Test
    @Order(3)
    void shouldUseGlobalConfigWhenNoConfigAtMethodLevel() {
        // Act
        final String name = sut.findCustomerNameById(NEW_CUSTOMER_ID);

        // Assert
        assertThat(name, equalTo(NEW_CUSTOMER_NAME));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(3));
    }

    @Test
    @Order(4)
    // tag::method_level_example[]
    @MyBatisTest(config = "mybatis-test-config.xml", script = "schema-alpha.sql", environment = "env_dev")
    void shouldUseSpecificConfigConfigWhenConfigAtMethodLevel() {
        // Act
        final String name = sut.findCustomerNameById(NEW_CUSTOMER_ID);

        // Assert
        assertThat(name, nullValue());

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(0));
    }
    // end::method_level_example[]

    @Test
    @Order(5)
    void shouldConfirmGlobalSessionStillAliveWhenNoMethodConfigDefined() {
        // Act
        final String customerOne = sut.findCustomerNameById(1);
        final String customerTwo = sut.findCustomerNameById(2);
        final String customerThree = sut.findCustomerNameById(NEW_CUSTOMER_ID);

        // Assert
        assertThat(customerOne, equalTo("alpha"));
        assertThat(customerTwo, equalTo("beta"));
        assertThat(customerThree, equalTo(NEW_CUSTOMER_NAME));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(3));
    }

}
