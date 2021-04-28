package lu.mms.common.quality.builders;

import org.apiguardian.api.API;
import org.mockito.Answers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Build a DataSource mock for given product name with {@link Answers#RETURNS_DEEP_STUBS} mockito answers.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "3.0.0"
)
public final class DataSourceMockBuilder {

    /** H2 key. */
    static final String H2 = "H2";
    /** Oracle key. */
    static final String ORACLE = "Oracle";

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceMockBuilder.class);

    private String productName;

    private DataSourceMockBuilder(final String productName) {
        this.productName = productName;
    }

    /**
     * @param productName the product name
     * @return the new builder
     */
    public static DataSourceMockBuilder newDataSourceMock(final String productName) {
        return new DataSourceMockBuilder(productName);
    }

    /**
     * @return The new H2 DataSource mock builder
     */
    public static DataSourceMockBuilder newH2Mock() {
        return new DataSourceMockBuilder(H2);
    }

    /**
     * @return The new Oracle DataSource mock builder
     */
    public static DataSourceMockBuilder newOracleMock() {
        return new DataSourceMockBuilder(ORACLE);
    }

    /**
     * Build the DataSource mock, with {@link DatabaseMetaData} and {@link Connection} mocks and stubs.
     * @return The prepared DataSource mock
     */
    public DataSource build() {
        final DataSource dataSourceMock = mock(DataSource.class, Answers.RETURNS_DEEP_STUBS);
        final DatabaseMetaData databaseMetaDataMock = mock(DatabaseMetaData.class);
        // initializing relevant mocks
        try (Connection connectionMock = mock(Connection.class)) {
            when(databaseMetaDataMock.getDatabaseProductName()).thenReturn(this.productName);
            when(connectionMock.getMetaData()).thenReturn(databaseMetaDataMock);
            when(dataSourceMock.getConnection()).thenReturn(connectionMock);
        } catch (SQLException exception) {
            LOGGER.error("Failed to initialize the DataSource stubs", exception);
        }
        return dataSourceMock;
    }

}
