package lu.mms.common.quality.assets.db.re;

import lu.mms.common.quality.assets.db.InMemoryDb;
import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.script.Ddl;
import lu.mms.common.quality.assets.db.re.script.Dml;
import lu.mms.common.quality.assets.mybatis.MyBatisMapperTest;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * This test check that SQL session is available for all test cases. <BR>
 * The test case
 */
@MyBatisMapperTest(
        dbEngine = InMemoryDb.H2_ORACLE,
        script = {
                "sql/schema.sql",
                "sql/data.sql"}
)
class ReverseEngineeringITest {

    private Schema schema;

    @BeforeEach
    void resolveJdbcTemplate(final SqlSessionFactory sqlSessionFactory) {
        final DataSource dataSource = sqlSessionFactory.openSession().getConfiguration().getEnvironment().getDataSource();
        schema = new ReverseEngineeringWizard(dataSource, "PUBLIC", 1)
                .withTable("CUSTOMER_ADDRESS", Expression.value("ID_ADDRESS").eq(1))
                .build();
    }

    @Test
    void shouldBuildDDLWhenSchemaProvided(){
        //  Arrange
        assumeFalse(schema.getTables().isEmpty());
        final Ddl ddl = Ddl.with(schema);

        // Act
        final String sql = ddl.build();

        // Assert
        assertThat(sql, containsString("DROP TABLE PUBLIC.CUSTOMER"));
        assertThat(sql, containsString("DROP TABLE PUBLIC.ADDRESS"));
        assertThat(sql, containsString("DROP TABLE PUBLIC.CUSTOMER_ADDRESS"));

        assertThat(sql, containsString("CREATE TABLE PUBLIC.CUSTOMER"));
        assertThat(sql, containsString("CREATE TABLE PUBLIC.ADDRESS"));
        assertThat(sql, containsString("CREATE TABLE PUBLIC.CUSTOMER_ADDRESS"));

        assertThat(sql, not(containsString("INSERT INTO")));
        assertThat(sql, not(containsString("DELETE FROM")));
    }

    @Test
    void shouldBuildDMLWhenSchemaProvided(){
        //  Arrange
        assumeFalse(schema.getTables().isEmpty());
        final Dml dml = Dml.with(schema);

        // Act
        final String sql = dml.build();

        // Assert
        assertThat(sql, containsString("INSERT INTO PUBLIC.CUSTOMER"));
        assertThat(sql, containsString("(5, 'gama', null, null)"));
        assertThat(sql, containsString("(6, 'x man', null, null)"));

        assertThat(sql, containsString("INSERT INTO PUBLIC.ADDRESS"));
        assertThat(sql, containsString("(1, 42, 'Rte de Luxembourg', '4590', 'Bascharage', 'Luxembourg', null)"));

        assertThat(sql, containsString("INSERT INTO PUBLIC.CUSTOMER_ADDRESS"));
        assertThat(sql, containsString("(5, 1)"));
        assertThat(sql, containsString("(6, 1)"));

        assertThat(sql, not(containsString("DROP TABLE")));
        assertThat(sql, not(containsString("CREATE TABLE")));
    }

}