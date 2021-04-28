package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.session.SqlSession;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static lu.mms.common.quality.assets.mybatis.MyBatisTestUtils.getSqlSessionFactory;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Mybatis JUnit 5 extension.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "3.0.0"
)
public class MyBatisExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    static final Map<Class<?>, SqlSession> SQL_CLASS_SESSIONS = new ConcurrentHashMap<>();
    static final Map<Method, SqlSession> SQL_METHOD_SESSIONS = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisExtension.class);

    @Override
    public void beforeAll(final ExtensionContext context) {
        super.beforeAll(context);

        context.getTestClass().ifPresent(MyBatisExtension::configureMyBatisTestClass);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        final Optional<Method> optMethod = context.getTestMethod();
        if (optMethod.isEmpty()) {
            return;
        }

        LOGGER.debug("Active sessions count = [{}], [<{}>]", SQL_CLASS_SESSIONS.size(), SQL_CLASS_SESSIONS);

        final Method method = optMethod.get();
        final SqlSession sqlSession = configureMyBatis(method, context.getTestClass().get());

        // Checking the connection status. The test case will be ignored if SqlSession is not valid.
        final String errorMessage = "[%s].[%s] Invalid SqlSession state.";
        assumeTrue(isValidSession(sqlSession),
            String.format(errorMessage, method.getDeclaringClass().getSimpleName(), method.getName()));

        context.getTestInstance().ifPresent(instance ->
            ReflectionUtils.getAllFields(instance.getClass(), this::isValidCandidate).forEach(field -> {
                final Optional<?> optMapper = findMapper(sqlSession, field);
                optMapper.ifPresent(mapper -> ReflectionTestUtils.setField(instance, field.getName(), mapper));
            })
        );
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        // close the method session
        context.getTestMethod().ifPresent(method -> closeSession(SQL_METHOD_SESSIONS.remove(method)));

        // close the class session in case of test isolation
        context.getTestClass().ifPresent(testClass -> {
            final MyBatisTest myBatis = testClass.getAnnotation(MyBatisTest.class);
            if (myBatis != null && myBatis.testIsolation()) {
                closeSession(SQL_CLASS_SESSIONS.remove(testClass));
            }
        });
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        super.afterAll(context);

        context.getTestClass().ifPresent(testClass -> {
            closeSession(SQL_CLASS_SESSIONS.get(testClass));
            SQL_CLASS_SESSIONS.remove(testClass);
        });
    }

    private boolean isValidCandidate(final Field field) {
        return field.isAnnotationPresent(InjectMapper.class) && field.getType().isAnnotationPresent(Mapper.class);
    }

    private static boolean isValidSession(final SqlSession sqlSession) {
        boolean isValid = false;
        try {
            isValid = sqlSession == null || (sqlSession.getConnection().isValid(0));
        } catch (SQLException ex) {
            LOGGER.error("Failed to check the connection. Status: [{}]", ex.getMessage());
        }
        return isValid;
    }

    private static void closeSession(final SqlSession sqlSession) {
        if (sqlSession != null) {
            sqlSession.close();
        }
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

    private static void configureMyBatisTestClass(final Class<?> targetClass) {
        final MyBatisTest myBatis = targetClass.getAnnotation(MyBatisTest.class);
        if (myBatis == null || myBatis.testIsolation()) {
            return;
        }

        LOGGER.debug("Configuring MyBatis for [{}].", targetClass.getName());
        // initialising the data source
        MyBatisTestUtils.initDataSource(targetClass, myBatis);
        LOGGER.debug("MyBatis configuration Applied.");

        // SQL Session creation for the given test class
        final SqlSession sqlSession = getSqlSessionFactory(targetClass).openSession(true);
        // saving the session, to be able to close it after test class execution
        SQL_CLASS_SESSIONS.put(targetClass, sqlSession);
    }

    private static SqlSession configureMyBatis(final Method method, final Class<?> testClass) {
        MyBatisTest myBatisTest = method.getAnnotation(MyBatisTest.class);
        if (myBatisTest == null) {
            // if not MyBatis annotation at method level, then we will use the one for whole test class
            final SqlSession sqlSession = SQL_CLASS_SESSIONS.get(testClass);
            if (sqlSession != null) {
                return sqlSession;
            }
            myBatisTest = testClass.getAnnotation(MyBatisTest.class);
            if (myBatisTest == null) {
                return null;
            }
        }

        if (!myBatisTest.testIsolation()) {
            LOGGER.warn("[testIsolation=false] is invalid at method level. Ignoring this config.");
        }

        LOGGER.debug("Configuring MyBatis for [{}].[{}(...)].",
            method.getDeclaringClass().getSimpleName(), method.getName());
        // initialising the data source
        MyBatisTestUtils.initDataSource(method, myBatisTest);
        LOGGER.debug("MyBatis configuration Applied.");

        // SQL Session creation for the given method
        final SqlSession sqlSession = getSqlSessionFactory(method).openSession(true);
        // saving the session, to be able to close it after method execution
        SQL_METHOD_SESSIONS.put(method, sqlSession);

        return sqlSession;
    }
}
