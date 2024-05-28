package lu.mms.common.quality.junit.assets.mybatis;

import lu.mms.common.quality.assets.db.InMemoryDb;
import lu.mms.common.quality.assets.db.MyBatisSqlSessionResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

import static lu.mms.common.quality.assets.mybatis.SessionFactoryUtils.newSqlSessionFactory;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * MybatisMapper JUnit 5 extension.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public class MyBatisMapperExtension extends MyBatisSqlSessionResolver implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisMapperExtension.class);

    @Override
    public void beforeAll(final ExtensionContext context) {
        super.beforeAll(context);
        final Class<?> testClass = context.getRequiredTestClass();
        final MyBatisMapperTest myBatisTest = testClass.getAnnotation(MyBatisMapperTest.class);
        if (myBatisTest == null) {
            return;
        }

        // insert 'sqlSession' in the context
        configureSqlSessionFactory(context, myBatisTest, testClass.getSimpleName())
                // retrieve shared Sql Session
                .map(factory -> initSqlSessionWithMigrationScripts(myBatisTest, factory))
                .ifPresent(SqlSession::close);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        final Method method = context.getRequiredTestMethod();
        final MyBatisMapperTest myBatisSettings = getMyBatisSettings(method);
        final SqlSession sqlSession;

        if (myBatisSettings != null) {
            sqlSession = configureSqlSessionFactory(context, myBatisSettings, method.getName())
                    .map(factory -> initSqlSessionWithMigrationScripts(myBatisSettings, factory))
                    .orElse(null);
        } else {
            // Open session from previous existing session factory
            final String sessionFactoryKey = computeSessionFactoryKey(method.getDeclaringClass().getSimpleName());
            sqlSession = Optional.ofNullable(getStore(context).get(sessionFactoryKey, SqlSessionFactory.class))
                    .map(factory -> factory.openSession(true))
                    .orElse(null);
        }

        // Open new session
        if (sqlSession == null) {
            return;
        }

        // Store the session in the context
        final String sessionKey = computeSessionKey(method.getName());
        getStore(context).put(sessionKey, sqlSession);

        // Checking the connection status. The test case will be ignored if SqlSession is not valid.
        final String errorMessage = "[%s].[%s] Invalid SqlSession state.";
        assumeTrue(isValidSession(sqlSession),
                String.format(errorMessage, method.getDeclaringClass().getSimpleName(), method.getName()));

        ReflectionUtils.getAllFields(method.getDeclaringClass(), this::isValidCandidate).forEach(field -> {
            final Optional<?> optMapper = findMapper(sqlSession, field);
            optMapper.ifPresent(mapper ->
                    ReflectionTestUtils.setField(context.getRequiredTestInstance(), field.getName(), mapper)
            );
        });
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        // close the method session
        final ExtensionContext.Store store = getStore(context);
        final Method method = context.getRequiredTestMethod();
        closeSqlSession(context, store, method.getName());

    }

    @Override
    public void afterAll(final ExtensionContext context) {
        super.afterAll(context);
        closeSqlSession(context, getStore(context), context.getRequiredTestClass().getSimpleName());
    }

    private void closeSqlSession(final ExtensionContext context, final ExtensionContext.Store store,
                                 final String sessionLabel) {
        final String sessionKey = computeSessionKey(sessionLabel);
        // get the SQL Session if exist
        final SqlSession sqlSession = store.get(sessionKey, SqlSession.class);
        // Close the SQL Session
        closeSession(context, sqlSession, sessionKey);

        // remove session factory if exists
        final String sessionFactoryKey = computeSessionFactoryKey(sessionLabel);
        getStore(context).remove(sessionFactoryKey, SqlSessionFactory.class);
    }

    private MyBatisMapperTest getMyBatisSettings(final Method method) {
        final MyBatisMapperTest methodSettings = method.getAnnotation(MyBatisMapperTest.class);
        final MyBatisMapperTest classSettings = method.getDeclaringClass().getAnnotation(MyBatisMapperTest.class);
        MyBatisMapperTest myBatisSettings = null;
        if (methodSettings != null) {
            myBatisSettings = methodSettings;
        } else if (classSettings != null && classSettings.testIsolation()) {
            myBatisSettings = classSettings;
        }
        return myBatisSettings;
    }

    private SqlSession initSqlSessionWithMigrationScripts(final MyBatisMapperTest myBatisTest,
                                                          final SqlSessionFactory factory) {
        final SqlSession session = factory.openSession(true);

        // apply migration script
        applyMigrationScripts(session, myBatisTest.dbEngine(), myBatisTest.script());
        LOGGER.debug("MyBatis configuration Applied.");
        return session;
    }

    private boolean isValidCandidate(final Field field) {
        return field.isAnnotationPresent(InjectMapper.class) && field.getType().isAnnotationPresent(Mapper.class);
    }

    private static boolean isValidSession(final SqlSession sqlSession) {
        boolean isValid = false;
        try {
            isValid = sqlSession == null || (sqlSession.getConnection().isValid(0));
        } catch (SQLException | ExecutorException ex) {
            LOGGER.error("Invalid SQL connection. Status: [{}]", ex.getMessage());
        }
        return isValid;
    }

    private static void closeSession(final ExtensionContext context, final SqlSession sqlSession,
                                     final String sessionKey) {
        if (sqlSession == null) {
            return;
        }
        sqlSession.close();
        getStore(context).remove(sessionKey, SqlSession.class);
    }

    private static Optional<?> findMapper(final SqlSession sqlSession, final Field field) {
        Optional<?> mapper = Optional.empty();
        if (sqlSession != null) {
            try {
                mapper = Optional.ofNullable(sqlSession.getMapper(field.getType()));
            } catch (BindingException ex) {
                LOGGER.error("Failed to find mapper for field [{}]: {}", field.getName(), ex.getMessage(), ex);
            }
        }
        return mapper;
    }

    private static Optional<SqlSessionFactory> configureSqlSessionFactory(final ExtensionContext extensionContext,
                                                                          final MyBatisMapperTest myBatisConfig,
                                                                          final String sessionFactoryLabel) {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        if (myBatisConfig == null) {
            LOGGER.warn("Missing MyBatis configuration for [{}].", sessionFactoryLabel);
            return Optional.empty();
        }
        LOGGER.debug("Configuring MyBatis for [{}].", sessionFactoryLabel);

        // Instantiate the SessionFactory
        final String sessionFactoryKey = computeSessionFactoryKey(sessionFactoryLabel);
        final SqlSessionFactory sqlSessionFactory = getStore(extensionContext).getOrComputeIfAbsent(
                sessionFactoryKey,
                id -> newSqlSessionFactory(testClass, id, myBatisConfig.dbEngine()),
                SqlSessionFactory.class
        );
        return Optional.of(sqlSessionFactory);
    }

    /**
     * Initialized the DataSource, using the provided configuration and sql scripts. <br>
     * Those scripts will be executed in the order they are provided.
     *
     * @param sqlSession The SQL session.
     * @param inMemoryDb The DB engine.
     * @param scripts    The migration scripts to execute
     * @throws IllegalArgumentException Thrown when failed to access a migration script
     */
    private static void applyMigrationScripts(final SqlSession sqlSession, InMemoryDb inMemoryDb, final String[] scripts) {

        final DataSource dataSource = sqlSession.getConfiguration().getEnvironment().getDataSource();
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Apply migration scripts
        if (ArrayUtils.isEmpty(scripts)) {
            LOGGER.warn("No script found in your config.");
            return;
        }

        // Execute migration scripts
        for (String migration: scripts) {
            if (StringUtils.isEmpty(migration)) {
                continue;
            }

            try (InputStream migrationStream = Resources.getResourceAsStream(migration)) {
                applyMigrationScripts(jdbcTemplate, migrationStream);
            } catch (Exception ex) {
                final String errorMsg = String.format(
                        "Failed to execute the migration script [%s]: [%s].", migration, ex.getMessage()
                );
                LOGGER.error(errorMsg, ex);
                throw new IllegalArgumentException(errorMsg, ex);
            }

            LOGGER.debug("Migration script [{}] applied.", migration);
        }

        // SQL Language Routines (PSM) / Java Language Routines (SQL/JRT)
        inMemoryDb.getSqlRoutines().forEach(jdbcTemplate::execute);
    }

    private static void applyMigrationScripts(final JdbcTemplate jdbcTemplate, final InputStream migStream)
                                                                                                throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(migStream, StandardCharsets.UTF_8))) {
            final String query = reader.lines().collect(Collectors.joining("\n"));
            jdbcTemplate.execute(query);
        }
    }

}
