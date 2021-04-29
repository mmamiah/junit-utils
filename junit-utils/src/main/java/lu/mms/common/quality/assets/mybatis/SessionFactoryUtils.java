package lu.mms.common.quality.assets.mybatis;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This class provide utils method to manage the {@link SessionFactoryUtils}.
 */
public final class SessionFactoryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactoryUtils.class);

    private static final Map<String, SqlSessionFactory> SQL_SESSION_FACTORIES = new ConcurrentHashMap<>();

    private static final String DATASOURCE_URL_TEMPLATE = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=Oracle;";

    private SessionFactoryUtils() {
        // hidden constructor
    }

    static void reset() {
        SQL_SESSION_FACTORIES.clear();
    }

    /**
     * Initialized the DataSource, using the provided configuration and sql scripts. <br>
     * Those scripts will be executed in the order they are provided.
     * @param sessionFactoryId The session factory ID.
     * @param myBatis The {@link MyBatisMapperTest} annotation
     * @return The {@link SqlSessionFactory} object
     * @throws IllegalStateException Thrown if the 'mybatisTestConfig' is blank.
     */
    static synchronized SqlSessionFactory initSessionFactory(final String sessionFactoryId,
                                                             final MyBatisMapperTest myBatis) {

        if (ArrayUtils.isEmpty(myBatis.script())) {
            LOGGER.warn("No script found in your config.");
        }

        // Instantiate the SessionFactory
        SqlSessionFactory sqlSessionFactory = SQL_SESSION_FACTORIES.get(sessionFactoryId);
        if (sqlSessionFactory == null) {
            sqlSessionFactory = SessionFactoryUtils.createSqlSessionFactory(sessionFactoryId, myBatis.mapperClass());
            SQL_SESSION_FACTORIES.put(sessionFactoryId, sqlSessionFactory);
        }

        // Instantiate the DataSource
        final DataSource dataSource;
        try {
            dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to initialize the DataSource.", ex);
        }

        // Execute migration scripts
        for (String migration: myBatis.script()) {
            LOGGER.debug("Applying migration script: [{}]", migration);
            MyBatisTestUtils.runScript(dataSource, migration);
            LOGGER.debug("Migration script [{}] applied.", migration);
        }

        return sqlSessionFactory;
    }

    /**
     * This method create a {@link SqlSessionFactory} as per the provided testcase name and the mapper to add to the
     * MyBatis configuration.
     * @param testCaseName The test case name
     * @param mapperClass The mapper classes to add to the configuration
     * @return The resulting session factory
     */
    public static SqlSessionFactory createSqlSessionFactory(final String testCaseName, final Class<?>[] mapperClass) {
        // Create an unpooled connection
        final UnpooledDataSource dataSource = new UnpooledDataSource();
        dataSource.setDriver("org.h2.Driver");
        dataSource.setUrl(String.format(DATASOURCE_URL_TEMPLATE, testCaseName));
        dataSource.setUsername("sa");

        //transaction
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();

        // Create an environment
        final Environment environment = new Environment(testCaseName, transactionFactory, dataSource);

        // Create a configuration
        final Configuration configuration = new Configuration(environment);

        //Open the hump rule
        configuration.setMapUnderscoreToCamelCase(true);

        // Join resources (Mapper interface)
        Stream.of(mapperClass).forEach(configuration::addMapper);

        // Get the session factory
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * Count rows in the table that matches the provided 'where' clause.
     * @param tableName The table to query
     * @param whereClause The where clause
     * @return The number of rows
     */
    public static int countRowsInTableWhere(final String tableName, final String whereClause) {
        final String sessionFactoryId = retrieveSessionFactoryId();
        final SqlSessionFactory sessionFactory = SQL_SESSION_FACTORIES.get(sessionFactoryId);
        try (Connection connection = sessionFactory.openSession(true).getConnection()) {
            String sqlQuery = "SELECT COUNT(*) FROM " + tableName;
            if (StringUtils.isNotEmpty(whereClause)) {
                sqlQuery += " WHERE " + whereClause;
            }
            try (ResultSet rs = connection.prepareStatement(sqlQuery).executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (final SQLException ex) {
            LOGGER.error("Failed to count the rows in table [{}] for clause [{}]: {}.",
                    tableName, whereClause, ex.getMessage(), ex);
        }
        return 0;
    }

    private static String retrieveSessionFactoryId() {
        String sessionFactoryId = null;
        for (StackTraceElement element: Thread.currentThread().getStackTrace()) {
            Optional<String> optId = retrieveSessionFactoryId(element.getMethodName());
            if (optId.isEmpty()) {
                optId = retrieveSessionFactoryId(element.getFileName());
            }
            if (optId.isPresent()) {
                sessionFactoryId = optId.get();
                break;
            }
        }
        return sessionFactoryId;
    }

    private static Optional<String> retrieveSessionFactoryId(final String search) {
        if (StringUtils.isEmpty(search)) {
            return Optional.empty();
        }
        return SQL_SESSION_FACTORIES.keySet().stream()
                .filter(search::contains)
                .findFirst();
    }

}
