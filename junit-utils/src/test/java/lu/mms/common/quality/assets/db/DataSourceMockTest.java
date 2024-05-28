package lu.mms.common.quality.assets.db;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.internal.util.MockUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

class DataSourceMockTest {

    private DataSourceMock sut;

    @Test
    void shouldBuildH2DataSourceMockWhenCallingH2Builder() throws SQLException {
        // Arrange
        sut = DataSourceMock.newH2Mock();

        // Act
        final DataSource dataSource = sut.build();

        // Assert
        assertThat(dataSource, notNullValue());
        assertThat(MockUtil.isMock(dataSource), equalTo(true));
        assertThat(MockUtil.getMockSettings(dataSource).getDefaultAnswer(), equalTo(Answers.RETURNS_DEEP_STUBS));

        final Connection connection = dataSource.getConnection();
        assertThat(connection, notNullValue());
        assertThat(MockUtil.isMock(connection), equalTo(true));

        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertThat(databaseMetaData, notNullValue());
        assertThat(databaseMetaData.getDatabaseProductName(), equalTo("H2"));
        assertThat(MockUtil.isMock(databaseMetaData), equalTo(true));
    }

    @Test
    void shouldBuildOracleDataSourceMockWhenCalliOracle2Builder() throws SQLException {
        // Arrange
        sut = DataSourceMock.newHsqlMock();

        // Act
        final DataSource dataSource = sut.build();

        // Assert
        assertThat(dataSource, notNullValue());
        assertThat(MockUtil.isMock(dataSource), equalTo(true));
        assertThat(MockUtil.getMockSettings(dataSource).getDefaultAnswer(), equalTo(Answers.RETURNS_DEEP_STUBS));

        final Connection connection = dataSource.getConnection();
        assertThat(connection, notNullValue());
        assertThat(MockUtil.isMock(connection), equalTo(true));

        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertThat(databaseMetaData, notNullValue());
        assertThat(databaseMetaData.getDatabaseProductName(), equalTo("HSQL"));
        assertThat(MockUtil.isMock(databaseMetaData), equalTo(true));
    }

    @Test
    void shouldBuildFreeHandDataSourceMockWhenUsingFreeHandBuilder() throws SQLException {
        // Arrange
        final String db2 = "db2";
        sut = DataSourceMock.newMock(db2);

        // Act
        final DataSource dataSource = sut.build();

        // Assert
        assertThat(dataSource, notNullValue());
        assertThat(MockUtil.isMock(dataSource), equalTo(true));
        assertThat(MockUtil.getMockSettings(dataSource).getDefaultAnswer(), equalTo(Answers.RETURNS_DEEP_STUBS));

        final Connection connection = dataSource.getConnection();
        assertThat(connection, notNullValue());
        assertThat(MockUtil.isMock(connection), equalTo(true));

        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertThat(databaseMetaData, notNullValue());
        assertThat(databaseMetaData.getDatabaseProductName(), equalTo(db2));
        assertThat(MockUtil.isMock(databaseMetaData), equalTo(true));
    }

}
