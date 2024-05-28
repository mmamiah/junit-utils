package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.db.InMemoryDb;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@MyBatisMapperTest(
        dbEngine = InMemoryDb.H2_ORACLE,
        script = {
        "sql/schema.sql",
        "sql/data-for-test_class.sql"}
)
class MyBatisSqlSessionResolverCTest {

    @Test
    void shouldProvideTheSqlSession(final SqlSession sqlSession) {
        // Arrange
        assumeTrue(sqlSession != null);
        final DataSource dataSource = sqlSession.getConfiguration()
                .getEnvironment()
                .getDataSource();
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Act
        final Integer countItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CUSTOMER", Integer.class);

        // Assert
        assertThat(countItems, equalTo(5));
    }

    @Test
    void shouldProvideTheSqlSessionFactory(final SqlSessionFactory sqlSessionFactory) {
        // Arrange
        assumeTrue(sqlSessionFactory != null);
        final DataSource dataSource = sqlSessionFactory.openSession(true)
                .getConfiguration()
                .getEnvironment()
                .getDataSource();
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Act
        final Integer countItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CUSTOMER", Integer.class);

        // Assert
        assertThat(countItems, equalTo(5));
    }

    @Test
    void shouldProvideTheJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        // Arrange
        assumeTrue(jdbcTemplate != null);

        // Act
        final Integer countItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CUSTOMER", Integer.class);

        // Assert
        assertThat(countItems, equalTo(5));
    }

}
