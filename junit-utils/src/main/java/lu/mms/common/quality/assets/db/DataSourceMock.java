package lu.mms.common.quality.assets.db;

import org.apiguardian.api.API;
import org.mockito.Answers;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Build a DataSource mock for given product name with {@link Answers#RETURNS_DEEP_STUBS} mockito answers.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "3.0.0"
)
public final class DataSourceMock {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceMock.class);

    private final String productName;
    private boolean isLenientConnection;

    private DataSourceMock(final String productName) {
        this.productName = productName;
    }

    /**
     * Create a new DataSource mock.
     * @param productName the product name
     * @return the new builder
     */
    public static DataSourceMock newMock(final String productName) {
        return new DataSourceMock(productName);
    }

    /**
     * @return The new H2 DataSource mock builder
     */
    public static DataSourceMock newH2Mock() {
        return new DataSourceMock(EmbeddedDatabaseType.H2.name());
    }

    /**
     * @return The new HSQL DataSource mock builder
     */
    public static DataSourceMock newHsqlMock() {
        return new DataSourceMock(EmbeddedDatabaseType.HSQL.name());
    }

    public DataSourceMock withLenientConnection() {
        this.isLenientConnection = true;
        return this;
    }

    /**
     * Build the DataSource mock, with {@link DatabaseMetaData} and {@link Connection} mocks and stubs.
     * @return The prepared DataSource mock
     */
    public DataSource build() {
        final DataSource datasourceMock = mock(DataSource.class, Answers.RETURNS_DEEP_STUBS);

        final MockSettings mockSettings;
        if (this.isLenientConnection) {
            mockSettings = Mockito.withSettings().strictness(Strictness.LENIENT);
        } else {
            mockSettings = Mockito.withSettings();
        }

        // initializing relevant mocks
        try (Connection connectionMock = mock(Connection.class, mockSettings)) {
            // DatabaseMetaData
            final DatabaseMetaData databaseMetaDataMock = mock(DatabaseMetaData.class);
            when(databaseMetaDataMock.getDatabaseProductName()).thenReturn(this.productName);
            when(connectionMock.getMetaData()).thenReturn(databaseMetaDataMock);

            // prepareStatement
            when(connectionMock.prepareStatement(anyString())).thenAnswer(inv -> initPreparedStatement());
            when(connectionMock.prepareStatement(anyString(), any(String[].class))).thenAnswer(inv -> initPreparedStatement());
            when(connectionMock.prepareStatement(anyString(), any(int[].class))).thenAnswer(inv -> initPreparedStatement());

            // Datasource
            when(datasourceMock.getConnection()).thenAnswer(inv -> connectionMock);
        } catch (SQLException exception) {
            LOGGER.error("Failed to initialize the DataSource stubs", exception);
        }
        return datasourceMock;
    }

    private static PreparedStatement initPreparedStatement() throws SQLException {
        // PreparedStatement
        final PreparedStatement statementMock = Mockito.mock(PreparedStatement.class);
        when(statementMock.executeQuery()).thenAnswer(inv -> initResultSetMock());
        when(statementMock.executeQuery(anyString())).thenAnswer(inv -> initResultSetMock());
        return statementMock;
    }

    private static ResultSet initResultSetMock() throws SQLException {
        // ResultSet
        final AtomicBoolean hasNext = new AtomicBoolean(true);
        final ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        when(resultSetMock.getString(anyString())).thenAnswer(inv -> inv.getArguments()[0]);
        when(resultSetMock.getString(anyInt())).thenAnswer(inv -> String.valueOf(inv.getArguments()[0]));
        when(resultSetMock.next()).thenAnswer(inv ->
                hasNext.getAndSet(false)
        );
        doAnswer(inv->
                hasNext.getAndSet(false)
        ).when(resultSetMock).close();
        return resultSetMock;
    }

}
