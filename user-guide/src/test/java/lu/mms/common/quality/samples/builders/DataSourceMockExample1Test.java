package lu.mms.common.quality.samples.builders;

import lu.mms.common.quality.builders.DataSourceMockBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.internal.util.MockUtil;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

// tag::example[]
@Configuration
class DataSourceMockExample1Test {

    @Test
    void shouldBuildH2DataSourceMockWhenCallingH2Builder() throws SQLException {
        // Act
        final DataSource dataSource = DataSourceMockBuilder.newH2Mock().build();

        // Assert
        assertThat(dataSource, notNullValue());
        assertThat(MockUtil.isMock(dataSource), equalTo(true));
        assertThat(MockUtil.getMockSettings(dataSource).getDefaultAnswer(), equalTo(Answers.RETURNS_DEEP_STUBS));

        final Connection connection = dataSource.getConnection();
        assertThat(MockUtil.isMock(connection), equalTo(true));

        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertThat(databaseMetaData.getDatabaseProductName(), equalTo("H2"));
        assertThat(MockUtil.isMock(databaseMetaData), equalTo(true));
    }
}
// end::example[]
