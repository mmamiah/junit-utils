package lu.mms.common.quality.assets.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MyBatisTestUtilsTest {

    private final String[] scripts = new String[] {"sql/schema.sql", "sql/data-for-test_class.sql"};

    private SqlSessionFactory sessionFactory;
    private SqlSession session;

    @BeforeEach
    void initMyBatisAnnotation() {
        sessionFactory = SessionFactoryUtils.initSessionFactory(getClass().getSimpleName(), getClass(), scripts);
        sessionFactory.getConfiguration().addMapper(EntityMapper.class);
        session = sessionFactory.openSession();
    }

    @Test
    void shouldInitSessionFactoryAndDataSourceWhenValidConfiguration() throws SQLException {
        // Arrange
        assumeTrue(sessionFactory.getConfiguration().hasMapper(EntityMapper.class));
        assumeFalse(session.getConnection().isClosed());

        // Act
        final EntityMapper mapper = session.getMapper(EntityMapper.class);

        // Assert
        assertThat(mapper, notNullValue());

        final String customerNotExist = mapper.findCustomerNameById(0);
        assertThat(customerNotExist, nullValue());

        final String customer = mapper.findCustomerNameById(1);
        assertThat(customer, notNullValue());


        final int countItems = SessionFactoryUtils.countRowsInTableWhere("CUSTOMER", "ID > 0");
        assertThat(countItems, equalTo(2));
    }

    @AfterEach
    void clearSessionFactory() {
        // Cleanup
        session.close();
        SessionFactoryUtils.clear();
    }

}
