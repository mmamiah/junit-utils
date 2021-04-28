package lu.mms.common.quality.assets.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyBatisTestUtilsTest {

    private static MyBatisTest myBatisTest;

    @BeforeAll
    static void initMyBatisAnnotation() {
        myBatisTest = getMyBatisTestAnnotation("mybatis-mapper-test.xml");
    }

    @Test
    void shouldNotFindSessionFactoryWhenNotDefined() {
        // Arrange

        // Act
        final SqlSessionFactory sessionFactory = MyBatisTestUtils.getSqlSessionFactory();

        // Assert
        assertThat(sessionFactory, nullValue());
    }

    @Test
    void shouldThrowExceptionWhenConfigIsNull() {
        // Arrange
        final MyBatisTest badConfig = getMyBatisTestAnnotation(null);

        // Act
        final Exception exception = assertThrows(
            Exception.class,
            () -> MyBatisTestUtils.initDataSource(getClass(), badConfig)
        );

        // Assert
        assertThat(exception, instanceOf(IllegalStateException.class));
    }

    @Test
    void shouldInitSessionFactoryAndDataSourceWhenValidConfiguration() {
        // Arrange
        MyBatisTestUtils.initDataSource(getClass(), myBatisTest);

        // Act
        final SqlSessionFactory sessionFactory = MyBatisTestUtils.getSqlSessionFactory();

        // Assert
        assertThat(sessionFactory, notNullValue());

        final int countItems = MyBatisTestUtils.countRowsInTableWhere("CUSTOMER", "ID > 0");
        assertThat(countItems, equalTo(2));
    }

    @AfterEach
    void clearSessionFactory() {
        MyBatisTestUtils.reset();
    }

    private static MyBatisTest getMyBatisTestAnnotation(final String config) {
        return new MyBatisTest() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MyBatisTest.class;
            }

            @Override
            public String config() {
                return config;
            }

            @Override
            public String[] script() {
                return new String[] {"schema-alpha.sql", "data-beta.sql"};
            }

            @Override
            public String environment() {
                return "";
            }

            @Override
            public boolean testIsolation() {
                return false;
            }
        };
    }

}
