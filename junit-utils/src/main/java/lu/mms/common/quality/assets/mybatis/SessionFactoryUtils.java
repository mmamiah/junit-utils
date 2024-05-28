package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.db.InMemoryDb;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provide utils method to manage the {@link SessionFactoryUtils}.
 */
public final class SessionFactoryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactoryUtils.class);

    private static final String SA = "SA";

    private SessionFactoryUtils() {
        // hidden constructor
    }

    /**
     * Initialized new SQL Session Factory, using the provided configuration. <br>
     * @param testClass The test class
     * @param sessionFactoryId The session factory ID.
     * @param inMemoryDb The database engine
     * @return The {@link SqlSessionFactory} object
     */
    public static SqlSessionFactory newSqlSessionFactory(final Class<?> testClass, final String sessionFactoryId,
                                                          final InMemoryDb inMemoryDb) {
        // Collect the mapper classes to register
        final List<Class<?>> mapperClasses = ReflectionUtils.getAllFields(
                testClass,
                ReflectionUtils.withAnnotation(InjectMapper.class)
        ).stream().map(Field::getType).collect(Collectors.toList());

        // Create the session factory
        return createSqlSessionFactory(sessionFactoryId, mapperClasses, inMemoryDb);
    }

    /**
     * This method create a {@link SqlSessionFactory} as per the provided testcase name and the mapper to add to the
     * MyBatis configuration.
     * @param testCaseName The test case name
     * @param mapperClasses The mapper classes to add to the configuration
     * @param inMemoryDb The database engine
     * @return The resulting session factory
     */
    public static SqlSessionFactory createSqlSessionFactory(final String testCaseName,
                                                            final List<Class<?>> mapperClasses,
                                                            final InMemoryDb inMemoryDb) {
        // Need to create a datasource with name having a random part to avoid reusing a previously create
        // datasource with same name. At this level we want a brand new one.
        final String dataSourceName = testCaseName + "_" + RandomStringUtils.randomAlphabetic(10);
        final EmbeddedDatabase dataSource = new EmbeddedDatabaseBuilder()
                .setName(String.format(inMemoryDb.getUrlTemplate(), dataSourceName))
                .setType(inMemoryDb.getDbType())
                .setScriptEncoding(StandardCharsets.UTF_8.name())
                .build();
        LOGGER.info("DataSource has been created with name = [{}].", dataSourceName);

        //transaction
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();

        // Create an environment
        final Environment environment = new Environment(testCaseName, transactionFactory, dataSource);

        // Create a configuration
        final Configuration configuration = new Configuration(environment);

        //Open the hump rule
        configuration.setMapUnderscoreToCamelCase(true);

        // Join resources (Mapper interface)
        mapperClasses.forEach(configuration::addMapper);

        // Get the session factory
        return new SqlSessionFactoryBuilder().build(configuration);
    }

}
