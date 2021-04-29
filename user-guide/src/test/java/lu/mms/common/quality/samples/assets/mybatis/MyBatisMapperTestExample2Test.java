package lu.mms.common.quality.samples.assets.mybatis;

import lu.mms.common.quality.assets.mybatis.InjectMapper;
import lu.mms.common.quality.assets.mybatis.MyBatisMapperTest;
import lu.mms.common.quality.samples.dao.CustomerMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

// tag::class_level_example[]
@MyBatisMapperTest(
    mapperClass = CustomerMapper.class,
    script = {"schema.sql", "data.sql"}
)
class MyBatisMapperTestExample2Test {

    @InjectMapper
    private CustomerMapper sut;

    @Test
    void shouldInitMyBatisMapperWhenClassConfig() {
        // Act
        final String name = sut.findCustomerNameById(1);

        // Assert
        assertThat(name, equalTo("alpha"));
    }
}
// end::class_level_example[]
