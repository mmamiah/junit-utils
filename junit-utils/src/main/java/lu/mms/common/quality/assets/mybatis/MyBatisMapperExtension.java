package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Mybatis JUnit 5 extension.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public class MyBatisMapperExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    /** The {@linkplain SqlSession SQL Session} by test class. */
    public static final Map<Class<?>, SqlSession> SQL_CLASS_SESSIONS = new ConcurrentHashMap<>();

    /** The {@linkplain SqlSession SQL Session} by test method. */
    public static final Map<Method, SqlSession> SQL_METHOD_SESSIONS = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisMapperExtension.class);

    @Override
    public void beforeAll(final ExtensionContext context) {
        super.beforeAll(context);
        configureMyBatisTestClassSession(context.getRequiredTestClass());
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        final Method method = context.getRequiredTestMethod();
        LOGGER.debug("Active sessions count = [{}], [<{}>]", SQL_CLASS_SESSIONS.size(), SQL_CLASS_SESSIONS);

        final SqlSession sqlSession = configureMyBatisTestMethodSession(method);

        // Checking the connection status. The test case will be ignored if SqlSession is not valid.
        final String errorMessage = "[%s].[%s] Invalid SqlSession state.";
        assumeTrue(isValidSession(sqlSession),
            String.format(errorMessage, method.getDeclaringClass().getSimpleName(), method.getName()));

        ReflectionUtils.getAllFields(context.getRequiredTestClass(), this::isValidCandidate).forEach(field -> {
            final Optional<?> optMapper = findMapper(sqlSession, field);
            optMapper.ifPresent(mapper ->
                    ReflectionTestUtils.setField(context.getRequiredTestInstance(), field.getName(), mapper)
            );
        });
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        // close the method session
        closeSession(SQL_METHOD_SESSIONS.remove(context.getRequiredTestMethod()));

        // close the class session in case of test isolation
        final Class<?> testClass = context.getRequiredTestClass();

        final MyBatisMapperTest myBatis = testClass.getAnnotation(MyBatisMapperTest.class);
        if (myBatis != null && myBatis.testIsolation()) {
            closeSession(SQL_CLASS_SESSIONS.remove(testClass));
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        super.afterAll(context);

        final Class<?> testClass = context.getRequiredTestClass();
        closeSession(SQL_CLASS_SESSIONS.get(testClass));
        SQL_CLASS_SESSIONS.remove(testClass);
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

    private static void configureMyBatisTestClassSession(final Class<?> targetClass) {
        final MyBatisMapperTest myBatis = targetClass.getAnnotation(MyBatisMapperTest.class);
        if (myBatis == null) {
            return;
        }

        LOGGER.debug("Configuring MyBatis for [{}].", targetClass.getName());
        // initialising the data source
        final SqlSessionFactory factory = SessionFactoryUtils.initSessionFactory(
                targetClass.getSimpleName(),
                targetClass,
                myBatis.script()
        );
        LOGGER.debug("MyBatis configuration Applied.");

        // SQL Session creation for the given test class
        final SqlSession sqlSession = factory.openSession(true);
        // saving the session, to be able to close it after test class execution
        SQL_CLASS_SESSIONS.put(targetClass, sqlSession);
    }

    private static SqlSession configureMyBatisTestMethodSession(final Method method) {
        MyBatisMapperTest myBatisTest = method.getAnnotation(MyBatisMapperTest.class);
        if (myBatisTest == null) {
            // if not MyBatis annotation at method level, then we will use the one for whole test class
            final SqlSession sqlSession = SQL_CLASS_SESSIONS.get(method.getDeclaringClass());
            if (sqlSession != null) {
                return sqlSession;
            }
            myBatisTest = method.getDeclaringClass().getAnnotation(MyBatisMapperTest.class);
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
        final SqlSessionFactory factory = SessionFactoryUtils.initSessionFactory(
                method.getName(),
                method.getDeclaringClass(),
                myBatisTest.script()
        );
        LOGGER.debug("MyBatis configuration Applied.");

        // SQL Session creation for the given method
        final SqlSession sqlSession = factory.openSession(true);
        // saving the session, to be able to close it after method execution
        SQL_METHOD_SESSIONS.put(method, sqlSession);

        return sqlSession;
    }
}
