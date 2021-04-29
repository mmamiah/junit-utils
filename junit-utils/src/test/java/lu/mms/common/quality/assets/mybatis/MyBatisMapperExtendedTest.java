package lu.mms.common.quality.assets.mybatis;

import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@MyBatisMapperTest(
        mapperClass = EntityMapper.class,
        script = {"schema.sql", "data-for-test_class.sql"}
)
class MyBatisMapperExtendedTest {

    @InjectMapper
    private EntityMapper sut;

    @Test
    void shouldFindValuesFromDefaultConfig() {
        // Act
        final String name = sut.findCustomerNameById(1);

        // Assert
        assertThat(name, equalTo("alpha"));

        final int itemCount = SessionFactoryUtils.countRowsInTableWhere("customer", "ID > 0");
        assertThat(itemCount, equalTo(2));
    }

    @Test
    @MyBatisMapperTest(mapperClass = EntityMapper.class, script = {"schema.sql", "data-for-test_method.sql"})
    void shouldInitMapperAtTestMethodLevel() {
        // Arrange

        // Act
        final String customer = sut.findCustomerNameById(3);

        // Assert
        assertThat(customer, containsString("method"));

        final int countItems = SessionFactoryUtils.countRowsInTableWhere("CUSTOMER", "ID > 0");
        assertThat(countItems, IsEqual.equalTo(3));
    }

    /**
     * Testcase to check that the use of {@link MyBatisMapperTest} at method level is using the specified context.
     * The datasource schema is initialized without any data.
     */
    @Test
    @MyBatisMapperTest(mapperClass = EntityMapper.class, script = "schema.sql")
    void shouldUseSpecificConfigWhenConfigAtMethodLevel() {
        // Arrange
        final int itemCount = SessionFactoryUtils.countRowsInTableWhere("customer", null);
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
    @MyBatisMapperTest(mapperClass = EntityMapper.class, script = "schema.sql", testIsolation = false)
    void shouldNotConsiderTestIsolationAtFalseForMethodConfig() {
        // Arrange
        final int itemCount = SessionFactoryUtils.countRowsInTableWhere("customer", null);
        assumeTrue(itemCount == 0);
        final Integer searchId = RandomUtils.nextInt(0, 1000);

        // Act
        final String name = sut.findCustomerNameById(searchId);

        // Assert
        assertThat(name, nullValue());
    }

}
