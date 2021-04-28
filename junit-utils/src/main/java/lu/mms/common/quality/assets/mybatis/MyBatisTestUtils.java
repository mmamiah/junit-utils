package lu.mms.common.quality.assets.mybatis;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for MyBatis test.
 */
public final class MyBatisTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisTestUtils.class);

    private static final Map<Object, SqlSessionFactory> SQL_SESSION_FACTORIES = new ConcurrentHashMap<>();

    private MyBatisTestUtils() {
        // hidden constructor
    }

    public static synchronized SqlSessionFactory getSqlSessionFactory() {
        return getSqlSessionFactory(retrieveSessionFactoryKey());
    }

    public static synchronized SqlSessionFactory getSqlSessionFactory(final Object target) {
        SqlSessionFactory sqlSessionFactory = null;
        if (!SQL_SESSION_FACTORIES.isEmpty() && target != null) {
            sqlSessionFactory = SQL_SESSION_FACTORIES.get(target);
        }
        return sqlSessionFactory;
    }

    static void reset() {
        SQL_SESSION_FACTORIES.clear();
    }

    /**
     * Count rows in the table that matches the provided 'where' clause.
     * @param tableName The table to query
     * @param whereClause The where clause
     * @return The number of rows
     */
    public static int countRowsInTableWhere(final String tableName, final String whereClause) {
        final Object sessionKey = retrieveSessionFactoryKey();
        try (Connection connection = getSqlSessionFactory(sessionKey).openSession(true).getConnection()) {
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

    /**
     * Initialized the DataSource, using the provided configuration and sql scripts. <br>
     * Those scripts will be executed in the order they are provided.
     * @param target The target object for which the data source should be initialized.
     * @param myBatis The my batis annotation
     * @throws IllegalStateException Thrown if the 'mybatisTestConfig' is blank.
     */
    static synchronized void initDataSource(final Object target, final MyBatisTest myBatis) {

        if (StringUtils.isEmpty(myBatis.config())) {
            throw new IllegalStateException("MyBatis config is missing.");
        }

        if (ArrayUtils.isEmpty(myBatis.script())) {
            LOGGER.warn("No script found in your config.");
        }

        // retrieve the environment
        String environment = null;
        if (StringUtils.isNotBlank(myBatis.environment())) {
            environment = myBatis.environment().trim();
        }

        // Instantiate the SessionFactory
        SqlSessionFactory sqlSessionFactory = SQL_SESSION_FACTORIES.get(target);
        if (sqlSessionFactory == null) {
            try (Reader reader = Resources.getResourceAsReader(myBatis.config())) {
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, environment);
                SQL_SESSION_FACTORIES.put(target, sqlSessionFactory);
            } catch (final IOException ex) {
                final StringBuilder msg = new StringBuilder(String.format("Failed to load the configuration [%s]",
                    myBatis.config()));
                if (StringUtils.isNotBlank(environment)) {
                    msg.append(" for environment [").append(environment).append("]");
                }
                throw new IllegalStateException(msg.toString(), ex);
            }
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
    }

    private static void runScript(final DataSource dataSource, final String resource) {
        if (org.springframework.util.StringUtils.isEmpty(resource)) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            final ScriptRunner runner = new ScriptRunner(connection);
            runner.setAutoCommit(true);
            runner.setStopOnError(true);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            applyScript(resource, runner);
        } catch (Exception ex) {
            final String msg = String.format("Failed to execute migration script [%s].", resource);
            throw new IllegalStateException(msg, ex);
        }
    }

    private static void applyScript(final String resource, final ScriptRunner runner) throws IOException {
        final Reader reader = Resources.getResourceAsReader(resource);
        LOGGER.debug("Script [{}] loaded.", resource);
        runner.runScript(reader);
        LOGGER.debug("Script [{}] executed.", resource);
    }

    private static Object retrieveSessionFactoryKey() {
        Object sessionKey = null;
        for (StackTraceElement element: Thread.currentThread().getStackTrace()) {
            Optional<Object> optSessionKey = findSessionKeyPerMethodName(element.getMethodName());
            if (optSessionKey.isEmpty()) {
                final String searchKey = ReflectionTestUtils.getField(element, "declaringClass").toString();
                optSessionKey = findSessionKeyPerDeclaringClass(searchKey);
            }
            if (optSessionKey.isPresent()) {
                sessionKey = optSessionKey.get();
                break;
            }
        }
        return sessionKey;
    }

    private static Optional<Object> findSessionKeyPerMethodName(final String methodName) {
        return SQL_SESSION_FACTORIES.keySet().stream()
            .filter(key -> key instanceof Method && ((Method) key).getName().equals(methodName))
            .findAny();
    }

    private static Optional<Object> findSessionKeyPerDeclaringClass(final String className) {
        return SQL_SESSION_FACTORIES.keySet().stream()
            .filter(key -> key instanceof Class && ((Class) key).getName().equals(className))
            .findAny();
    }


}
