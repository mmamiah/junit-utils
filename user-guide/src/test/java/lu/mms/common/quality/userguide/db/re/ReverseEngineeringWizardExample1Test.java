package lu.mms.common.quality.userguide.db.re;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lu.mms.common.quality.assets.db.re.Expression;
import lu.mms.common.quality.assets.db.re.ReverseEngineeringWizard;
import lu.mms.common.quality.assets.db.re.schema.Schema;
import lu.mms.common.quality.assets.db.re.script.Ddl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@Disabled("Lib documentation")
// tag::example[]
class ReverseEngineeringWizardExample1Test {

    private DataSource dataSource;

    @BeforeEach
    void init() {
        final HikariConfig dbConfig = new HikariConfig();
        dbConfig.setUsername("U_NAME");
        dbConfig.setPassword("pwd");
        dbConfig.addDataSourceProperty("poolName", "test_pool");
        dbConfig.addDataSourceProperty("maximumPoolSize", 30);
        dbConfig.setJdbcUrl("jdbc:oracle:thin:@//my_data_base:1521/DB_NAME");
        dataSource = new HikariDataSource(dbConfig);
    }

    /**
     * Should create mig script in the same package when package provider exists.
     * We will be using DDL for this test, but the same works for DML as well.
     */
    @Test
    void shouldCreateMigScriptInSamePackageWhenPackageProviderExists(){
        //  Arrange
        final Schema schema = new ReverseEngineeringWizard(
                dataSource,
                "SCHEMA_NAME"
        ).withTable(
                "CUSTOMER",
                // match in the CUSTOMER table
                Expression
                    .value("CD_TYPE").eq("AXE")
                    .and(Expression.value("CD_LANGUE").in("FR", "EN"))
                    .or(Expression.value("ID_CUST").between(468, 700)),
                Expression.value("CITY").in("Luxembourg", "Madrid", "Paris")
        ).build();
        assumeFalse(schema.getTables().isEmpty());

        // DDL with package provider
        final Ddl ddl = Ddl.with(schema, this.getClass());

        // Act
        // create the DDL file in the same package as this class
        final boolean ddlCreated = ddl.createFile();

        // Assert
        assertThat(ddlCreated, equalTo(true));
    }

    /**
     * Should create mig script in the 'test/resources/sql' folder when no package provider.
     * We will be using DDL for this test, but the same works for DML as well.
     */
    @Test
    void shouldCreateMigScriptInSamePackageWhenMissingPackageProvider(){
        //  Arrange
        final Schema schema = new ReverseEngineeringWizard(
                dataSource,
                "SCHEMA_NAME"
        ).withTable(
                "CUSTOMER",
                // match in the CUSTOMER table
                Expression.value("CD_TYPE").eq("AXE"),
                Expression.value("CD_LANGUE").in("FR", "EN", "LU"),
                Expression.value("ID_CUST").between(468, 700)
        ).build();
        assumeFalse(schema.getTables().isEmpty());

        // DML without package provider
        final Ddl ddl = Ddl.with(schema);

        // Act
        // create the DDL file in the 'test/resources/sql' folder
        final boolean ddlCreated = ddl.createFile();

        // Assert
        assertThat(ddlCreated, equalTo(true));
    }

}
// end::example[]