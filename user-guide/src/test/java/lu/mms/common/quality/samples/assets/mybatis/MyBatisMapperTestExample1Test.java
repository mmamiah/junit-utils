package lu.mms.common.quality.samples.assets.mybatis;

import lu.mms.common.quality.assets.mybatis.InjectMapper;
import lu.mms.common.quality.assets.mybatis.MyBatisMapperTest;
import lu.mms.common.quality.assets.mybatis.SessionFactoryUtils;
import lu.mms.common.quality.samples.dao.CustomerMapper;
import lu.mms.common.quality.samples.dao.NonMapperBean;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@MyBatisMapperTest(
    testIsolation = false,
    script = {"schema.sql", "data.sql"}
)
@TestMethodOrder(OrderAnnotation.class)
class MyBatisMapperTestExample1Test {

    private static final String NEW_CUSTOMER_NAME = "new_customer";
    // ID not in 'data.sql'
    private static final Integer NEW_CUSTOMER_ID = 5000;
    private static final String CUSTOMER = "customer";
    private static final String ID_GREATHER_THAN_0 = "ID > 0";

    @InjectMapper
    private CustomerMapper sut;

    @InjectMapper
    private NonMapperBean nonMapperBean;

    @Test
    @Order(1)
    void shouldInitMyBatisMapperWhenClassConfig() {
        // Arrange
        final int firstIdInDB = 1; // ID in 'data.sql'

        // Act
        final String name = sut.findCustomerNameById(firstIdInDB);

        // Assert
        assertThat(name, equalTo("alpha"));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = SessionFactoryUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @Order(2)
    void shouldInsertCustomerInGlobalSessionWhenNoNewConfigDefined() {
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

        final int itemCount = SessionFactoryUtils.countRowsInTableWhere(CUSTOMER, null);
        assertThat(itemCount, equalTo(3));
    }

    @Test
    @Order(3)
    void shouldUseGlobalConfigWhenNoConfigAtMethodLevel() {
        // Arrange
        final int firstIdInDB = 1; // ID in 'data.sql'

        // Act
        final String name = sut.findCustomerNameById(firstIdInDB);

        // Assert
        assertThat(name, equalTo("alpha"));

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = SessionFactoryUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @Order(4)
    // tag::method_level_example[]
    @MyBatisMapperTest(script = "schema.sql")
    void shouldUseSpecificConfigConfigWhenConfigAtMethodLevel() {
        // Arrange
        final int firstIdInDB = 1; // ID in 'data.sql'

        // Act
        final String name = sut.findCustomerNameById(firstIdInDB);

        // Assert
        assertThat(name, nullValue());

        // Mock not initialized as not declared in my batis config.
        assertThat(nonMapperBean, nullValue());

        final int itemCount = SessionFactoryUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(0));
    }
    // end::method_level_example[]

    @Test
    @Order(5)
    void shouldConfirmGlobalSessionStillAliveWhenNoMethodConfigDefined() {
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

        final int itemCount = SessionFactoryUtils.countRowsInTableWhere(CUSTOMER, ID_GREATHER_THAN_0);
        assertThat(itemCount, equalTo(2));
    }

}
