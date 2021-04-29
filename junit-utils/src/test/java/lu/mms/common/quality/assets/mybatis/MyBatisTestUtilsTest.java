package lu.mms.common.quality.assets.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MyBatisTestUtilsTest {

    private static MyBatisMapperTest myBatisTest;

    @BeforeAll
    static void initMyBatisAnnotation() {
        myBatisTest = getMyBatisTestAnnotation(EntityMapper.class);
    }

    @Test
    void shouldThrowExceptionWhenConfigIsNull() {
        // Arrange
        final MyBatisMapperTest badConfig = getMyBatisTestAnnotation(null);

        // Act
        final Exception exception = assertThrows(
            Exception.class,
            () -> SessionFactoryUtils.initSessionFactory(getClass().getSimpleName(), badConfig)
        );

        // Assert
        assertThat(exception, notNullValue());
    }

    @Test
    void shouldInitSessionFactoryAndDataSourceWhenValidConfiguration() {
        // Arrange
        final SqlSessionFactory sessionFactory = SessionFactoryUtils.initSessionFactory(getClass().getSimpleName(), myBatisTest);
        assumeTrue(sessionFactory != null);

        // Act
        final SqlSession session = sessionFactory.openSession();

        // Assert
        final EntityMapper mapper = session.getMapper(EntityMapper.class);
        assertThat(mapper, notNullValue());

        final String customerNotExist = mapper.findCustomerNameById(0);
        assertThat(customerNotExist, nullValue());

        final String customer = mapper.findCustomerNameById(1);
        assertThat(customer, notNullValue());


        final int countItems = SessionFactoryUtils.countRowsInTableWhere("CUSTOMER", "ID > 0");
        assertThat(countItems, equalTo(2));

        // Cleanup
        session.close();
    }

    @AfterEach
    void clearSessionFactory() {
        SessionFactoryUtils.reset();
    }

    private static MyBatisMapperTest getMyBatisTestAnnotation(final Class<?> mapperClass) {
        return new MyBatisMapperTest() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MyBatisMapperTest.class;
            }

            @Override
            public String[] script() {
                return new String[] {"schema.sql", "data-for-test_class.sql"};
            }

            @Override
            public boolean testIsolation() {
                return false;
            }

            @Override
            public Class<?>[] mapperClass() {
                return new Class[] {mapperClass};
            }
        };
    }

}
