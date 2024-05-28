package lu.mms.common.quality.junit.assets.db;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *  This extension make the {@linkplain SqlSession SQL Session} available for the test method/case.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public class MyBatisSqlSessionResolver extends JunitUtilsExtension implements ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSqlSessionResolver.class);

    private static final String SESSION_KEY = "Session_";
    private static final String SESSION_FACTORY_KEY = "SessionFactory_";

    private final Map<Class<?>, Function<ExtensionContext, ?>> sessionCatalog;

    /**
     * MyBatisSqlSessionResolver default constructor.
     */
    public MyBatisSqlSessionResolver() {
        this.sessionCatalog = new HashMap<>();
        this.sessionCatalog.put(SqlSessionFactory.class, getSqlSessionFactoryFunction());
        this.sessionCatalog.put(SqlSession.class, getSqlSessionFunction());
        this.sessionCatalog.put(JdbcTemplate.class, getJdbcTemplateFunction());
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return this.sessionCatalog.containsKey(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        if (extensionContext == null) {
            LOGGER.warn("The extension context is missing.");
            return null;
        }

        return this.sessionCatalog.get(parameterContext.getParameter().getType())
                .apply(extensionContext);
    }

    /**
     * @param extensionContext The extension
     * @return  The DateSource label
     */
    public static String computeContextSessionKey(final ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
                .map(method -> computeSessionKey(method.getName()))
                .orElse(computeSessionKey(extensionContext.getRequiredTestClass().getSimpleName()));
    }

    /**
     * @param extensionContext The extension
     * @return  The DateSource label
     */
    public static String computeContextSessionFactoryKey(final ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
                .map(Method::getDeclaringClass)
                .map(testClass -> computeSessionFactoryKey(testClass.getSimpleName()))
                .orElse(computeSessionKey(extensionContext.getRequiredTestClass().getSimpleName()));
    }

    /**
     * Compute the {@linkplain SqlSession SQL Session} label to be used in the extension context.
     * @param key   The key to use to compute the label
     * @return  The SQL Session label
     */
    public static String computeSessionKey(final String key) {
        return SESSION_KEY + key;
    }

    /**
     * Compute the {@linkplain SqlSessionFactory SQL Session Factory} label to be used in the extension context.
     * @param key   The key to use to compute the label
     * @return  The SQL Session Factory label
     */
    public static String computeSessionFactoryKey(final String key) {
        return SESSION_FACTORY_KEY + key;
    }

    /**
     * Function to retrieve the {@link SqlSessionFactory} from the {@link ExtensionContext.Store}.
     * @return {@link SqlSessionFactory} object
     */
    private static Function<ExtensionContext, SqlSessionFactory> getSqlSessionFactoryFunction() {
        return extensionContext -> {
            final String sessionFactoryLabel = computeContextSessionFactoryKey(extensionContext);
            final ExtensionContext.Store store = getStore(extensionContext);
            return store.get(sessionFactoryLabel, SqlSessionFactory.class);
        };
    }

    /**
     * Function to retrieve the {@link SqlSession} from the {@link ExtensionContext.Store}.
     * @return {@link SqlSession} object
     */
    private static Function<ExtensionContext, SqlSession> getSqlSessionFunction() {
        return extensionContext -> {
            final String sessionLabel = computeContextSessionKey(extensionContext);
            final ExtensionContext.Store store = getStore(extensionContext);
            return store.get(sessionLabel, SqlSession.class);
        };
    }

    /**
     * Function to instantiate the {@link JdbcTemplate}, using the {@link DataSource} from the {@link ExtensionContext}
     * ({@link SqlSession}).
     * @return {@link JdbcTemplate} object
     */
    private static Function<ExtensionContext, JdbcTemplate> getJdbcTemplateFunction() {
        return extensionContext -> {
            final SqlSession sqlSession = getSqlSessionFunction().apply(extensionContext);
            return new JdbcTemplate(sqlSession.getConfiguration().getEnvironment().getDataSource());
        };
    }

}
