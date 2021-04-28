package lu.mms.common.quality.assets.mybatis;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@MyBatisTest(config = "mybatis-mapper-test.xml", script = {"schema-alpha.sql", "data-beta.sql"})
class MyBatisMethodIsolationTest {

    private static final Integer NEW_CUSTOMER_ID = 3;

    @InjectMapper
    private EntityMapper sut;

    @Test
    void shouldFindValuesFromDefaultConfig() {
        // Act
        final String name = sut.findCustomerNameById(1);

        // Assert
        assertThat(name, equalTo("alpha"));

        // Mock not initialized as not declared in my batis config.
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere("customer", "ID > 0");
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @MyBatisTest(config = "mybatis-mapper-test.xml", script = "schema-alpha.sql")
    void shouldUseSpecificConfigConfigWhenConfigAtMethodLevel() {
        // Act
        final String name = sut.findCustomerNameById(NEW_CUSTOMER_ID);

        // Assert
        assertThat(name, nullValue());

        // Mock not initialized as not declared in my batis config.
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere("customer", "ID > 0");
        assertThat(itemCount, equalTo(0));
    }

    @Test
    @MyBatisTest(config = "mybatis-mapper-test.xml", script = "schema-alpha.sql", testIsolation = false)
    void shouldNotConsiderTestIsolationAtFalseForMethodConfig() {
        // Act
        final String name = sut.findCustomerNameById(NEW_CUSTOMER_ID);

        // Assert
        assertThat(name, nullValue());

        // Mock not initialized as not declared in my batis config.
        assertThat(MyBatisTestUtils.getSqlSessionFactory(getClass()), nullValue());
        assertThat(MyBatisTestUtils.getSqlSessionFactory(), notNullValue());

        final int itemCount = MyBatisTestUtils.countRowsInTableWhere("customer", "ID > 0");
        assertThat(itemCount, equalTo(0));
    }

}
