package lu.mms.common.quality.assets.mybatis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Test that Mybatis configuration is properly applied.
 */
@ExtendWith(MockitoExtension.class)
class MyBatisExtensionTest {

    private final MyBatisExtension sut = new MyBatisExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    private TestcaseItem testInstance;

    @BeforeEach
    void init() {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        assumeTrue(MyBatisExtension.SQL_METHOD_SESSIONS.isEmpty(), "No Method Session should be defined");
    }

    @Test
    void shouldDoNothingOnBeforeAllAndBeforeEachWhenMissingAnnotation() throws NoSuchMethodException {
        // Arrange
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(EntityMapper.class));

        // we just resolve any existing method. we do not care if it is a real test one or not.
        final Method testMethod = EntityMapper.class.getMethod("findCustomerNameById", Integer.class);
        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testMethod));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS.isEmpty(), equalTo(true));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS.isEmpty(), equalTo(true));
    }

    @Test
    void shouldApplyMybatisConfigurationOnBeforeAll() {
        // Arrange
        testInstance = new MyBatisTestClassItem();
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));

        // Act
        sut.beforeAll(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS, aMapWithSize(1));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS.isEmpty(), equalTo(true));

        // At @BeforeAll level, we do not instantiate the @Mapper.
        assertThat(testInstance.getMapper(), nullValue());
    }

    @Test
    void shouldInitMybatisConfigurationAtClassLevelOnBeforeEachWhenNoTestMethodDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestClassItem();
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));
        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));
        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS, aMapWithSize(1));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS.isEmpty(), equalTo(true));
        assertThat(testInstance.getMapper(), nullValue());
    }

    @Test
    void shouldInitMybatisConfigurationAtMethodLevelOnBeforeEachWhenMethodConfigDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestMethodItem();
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));
        when(extensionContextMock.getTestInstance()).thenReturn(Optional.of(testInstance));

        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS.isEmpty(), equalTo(true));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS, aMapWithSize(1));
        assertThat(testInstance.getMapper(), notNullValue());
    }

    @Test
    void shouldInitMybatisConfigurationOnBeforeEachWhenHybridConfigDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestHybridItem();
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));
        when(extensionContextMock.getTestInstance()).thenReturn(Optional.of(testInstance));

        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS.isEmpty(), equalTo(true));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS, aMapWithSize(1));
        assertThat(testInstance.getMapper(), notNullValue());
    }

    @Test
    void shouldApplyMybatisConfigurationOnBeforeEachWhenClassConfigAndMethodConfigDefined()
                                                                                    throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestHybridItemNoClassLevelTestIsolation();
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));
        when(extensionContextMock.getTestInstance()).thenReturn(Optional.of(testInstance));

        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS, aMapWithSize(1));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS, aMapWithSize(1));
        assertThat(testInstance.getMapper(), notNullValue());
    }

    @Test
    void shouldIgnoreMethodTestIsolationConfigWhenClassConfigAndMethodConfigAndNoTestIsolationAtMethodLevelDefined()
        throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestHybridItemNoMethodLevelTestIsolation();
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));
        when(extensionContextMock.getTestInstance()).thenReturn(Optional.of(testInstance));

        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS, aMapWithSize(1));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS, aMapWithSize(1));
        assertThat(testInstance.getMapper(), notNullValue());
    }

    @ParameterizedTest
    @ValueSource(classes = {
        TestCaseScriptNotFound.class, TestCaseScriptNotFound.class, TestBlankConfig.class
    })
    void shouldThrowExceptionWhenInvalidConfig(final Class<TestcaseItem> testcaseClass) throws NoSuchMethodException,
                                            IllegalAccessException, InvocationTargetException, InstantiationException {
        // Arrange
        testInstance = ReflectionUtils.accessibleConstructor(testcaseClass).newInstance((Object[]) null);
        when(extensionContextMock.getTestClass()).thenReturn(Optional.of(testInstance.getClass()));
        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        final Exception exception = assertThrows(Exception.class, () -> sut.beforeEach(extensionContextMock));

        // Assert
        assertThat(exception, instanceOf(IllegalStateException.class));
    }

    @AfterEach
    void closeTestcaseSessions() {
        sut.afterEach(extensionContextMock);
        sut.afterAll(extensionContextMock);
    }

    @AfterAll
    static void afterAllTestcase() {
        // ensure that all Sessions have been closes
        assertThat(MyBatisExtension.SQL_CLASS_SESSIONS.isEmpty(), equalTo(true));
        assertThat(MyBatisExtension.SQL_METHOD_SESSIONS.isEmpty(), equalTo(true));
    }

    // Target context / config / entities and so on  ------------------------------------------------

    static class TestcaseItem {
        @InjectMapper
        private EntityMapper entityMapper;

        EntityMapper getMapper() {
            return entityMapper;
        }

        Method getTestMethod() throws NoSuchMethodException {
            return getClass().getMethod("exampleOfTestMethod");
        }

        public void exampleOfTestMethod() {
            Assertions.fail();
        }
    }

    @MyBatisTest(
        testIsolation = false,
        config = "mybatis-mapper-test.xml",
        script = {"schema-alpha.sql", "data-beta.sql"}
    )
    private static class MyBatisTestClassItem extends TestcaseItem {

    }

    private static class MyBatisTestMethodItem extends TestcaseItem {
        @MyBatisTest(config = "mybatis-mapper-test.xml", script = {"schema-alpha.sql", "data-beta.sql"})
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisTest(
        config = "mybatis-mapper-test.xml",
        script = {"schema-alpha.sql", "data-beta.sql"}
    )
    private static class MyBatisTestHybridItem extends TestcaseItem {
        @MyBatisTest(config = "mybatis-mapper-test.xml", script = {"schema-alpha.sql", "data-beta.sql"})
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisTest(
        testIsolation = false,
        config = "mybatis-mapper-test.xml",
        script = {"schema-alpha.sql", "data-beta.sql"}
    )
    private static class MyBatisTestHybridItemNoClassLevelTestIsolation extends TestcaseItem {
        @MyBatisTest(config = "mybatis-mapper-test.xml", script = {"schema-alpha.sql", "data-beta.sql"})
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisTest(
        testIsolation = false, // to force class session creation
        config = "mybatis-mapper-test.xml",
        script = {"schema-alpha.sql", "data-beta.sql"}
    )
    private static class MyBatisTestHybridItemNoMethodLevelTestIsolation extends TestcaseItem {
        @MyBatisTest(
            testIsolation = false,
            config = "mybatis-mapper-test.xml",
            script = {"schema-alpha.sql", "data-beta.sql"}
        )
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisTest(config = "mybatis-mapper-test.xml", script = "bad-data.sql")
    private static class TestCaseScriptNotFound extends TestcaseItem {

    }

    @MyBatisTest(config = "config-not-found.xml", script = {"schema-alpha.sql", "data-beta.sql"})
    private static class TestConfigNotFound extends TestcaseItem {

    }

    @MyBatisTest(config = "", script = {"schema-alpha.sql", "data-beta.sql"})
    private static class TestBlankConfig extends TestcaseItem {

    }


}
