package lu.mms.common.quality.assets.mybatis;

import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import lu.mms.common.quality.assets.db.MyBatisSqlSessionResolver;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Test that Mybatis configuration is properly applied.
 */
@ExtendWith(MockitoExtension.class)
class MyBatisMapperExtensionTest {

    private final MyBatisMapperExtension sut = new MyBatisMapperExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    private TestcaseItem testInstance;

    @BeforeEach
    void init() {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);
        final SqlSession session = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(this.getClass().getSimpleName()), SqlSession.class);
        assumeTrue(session == null, "No Method Session should be defined");
    }

    @AfterEach
    void closeTestcaseSessions() {
        sut.afterEach(extensionContextMock);
        sut.afterAll(extensionContextMock);
    }

    @Test
    void shouldDoNothingWhenMissingAnnotation(final TestInfo testInfo) throws NoSuchMethodException {
        // Arrange
        when(extensionContextMock.getRequiredTestInstance()).thenAnswer(inv -> this);
        when(extensionContextMock.getRequiredTestClass()).thenAnswer(inv -> getClass());
        final Method testMethod = testInfo.getTestMethod().orElseThrow();
        when(extensionContextMock.getRequiredTestMethod()).thenAnswer(inv -> testMethod);

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final SqlSession session = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testMethod.getName()), SqlSession.class);
        assertThat(session, nullValue());
    }

    @Test
    void shouldApplyMybatisConfigurationOnBeforeAll() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestClassItem();
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) testInstance.getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(testInstance);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInstance.getTestMethod());

        // Act
        sut.beforeAll(extensionContextMock);

        // Assert
        final SqlSession classSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getClass().getSimpleName()), SqlSession.class);
        assertThat(classSession, nullValue());

        final SqlSession methodSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getTestMethod().getName()), SqlSession.class);
        assertThat(methodSession, nullValue());

        // At @BeforeAll level, we do not instantiate the @Mapper.
        assertThat(testInstance.getMapper(), nullValue());
    }

    @Test
    void shouldNotInitMybatisConfigurationAtClassLevelOnBeforeEachWhenNoTestMethodDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestClassItem();
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) testInstance.getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(testInstance);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInstance.getTestMethod());

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final SqlSession classSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getClass().getSimpleName()), SqlSession.class);
        assertThat(classSession, nullValue());

        final SqlSession methodSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getTestMethod().getName()), SqlSession.class);
        assertThat(methodSession, nullValue());
        assertThat(testInstance.getMapper(), nullValue());
    }

    @Test
    void shouldInitMybatisConfigurationAtMethodLevelOnBeforeEachWhenMethodConfigDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestMethodItem();
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) testInstance.getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(testInstance);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInstance.getTestMethod());

        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final SqlSession classSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getClass().getSimpleName()), SqlSession.class);
        assertThat(classSession, nullValue());

        final SqlSession methodSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getTestMethod().getName()), SqlSession.class);
        assertThat(methodSession, notNullValue());

        assertThat(testInstance.getMapper(), notNullValue());
    }

    @Test
    void shouldInitMybatisConfigurationOnBeforeEachWhenHybridConfigDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestHybridItem();
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) testInstance.getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(testInstance);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInstance.getTestMethod());

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final SqlSession classSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getClass().getSimpleName()), SqlSession.class);
        assertThat(classSession, nullValue());

        final SqlSession methodSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getTestMethod().getName()), SqlSession.class);
        assertThat(methodSession, notNullValue());

        assertThat(testInstance.getMapper(), notNullValue());
    }

    @Test
    void shouldApplyMybatisConfigurationOnBeforeEachWhenClassConfigAndMethodConfigDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestHybridItemNoClassLevelTestIsolation();
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) testInstance.getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(testInstance);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInstance.getTestMethod());

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final SqlSession classSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getClass().getSimpleName()), SqlSession.class);
        assertThat(classSession, nullValue());

        final SqlSession methodSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getTestMethod().getName()), SqlSession.class);
        assertThat(methodSession, notNullValue());

        assertThat(testInstance.getMapper(), notNullValue());
    }

    @Test
    void shouldIgnoreMethodTestIsolationConfigWhenClassConfigAndMethodConfigAndNoTestIsolationAtMethodLevelDefined() throws NoSuchMethodException {
        // Arrange
        testInstance = new MyBatisTestNoMethodLevelTestIsolation();
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) testInstance.getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(testInstance);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInstance.getTestMethod());

        when(extensionContextMock.getTestMethod()).thenReturn(Optional.of(testInstance.getTestMethod()));

        sut.beforeAll(extensionContextMock);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final SqlSession classSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getClass().getSimpleName()), SqlSession.class);
        assertThat(classSession, nullValue());

        final SqlSession methodSession = storeSpy.get(MyBatisSqlSessionResolver.computeSessionKey(testInstance.getTestMethod().getName()), SqlSession.class);
        assertThat(methodSession, notNullValue());

        assertThat(testInstance.getMapper(), notNullValue());
    }

    // Target context / config / entities and so on  ------------------------------------------------

    abstract static class TestcaseItem {

        abstract EntityMapper getMapper();

        Method getTestMethod() throws NoSuchMethodException {
            return getClass().getMethod("exampleOfTestMethod");
        }

        public void exampleOfTestMethod() {
            Assertions.fail();
        }
    }

    @MyBatisMapperTest
    private static class MyBatisTestClassItem extends TestcaseItem {
        @InjectMapper
        private EntityMapper entityMapper;

        @Override
        EntityMapper getMapper() {
            return entityMapper;
        }

    }

    private static class MyBatisTestMethodItem extends TestcaseItem {
        @InjectMapper
        private EntityMapper entityMapper;

        @Override
        EntityMapper getMapper() {
            return entityMapper;
        }

        @Override
        @MyBatisMapperTest
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisMapperTest(testIsolation = false)
    private static class MyBatisTestHybridItem extends TestcaseItem {
        @InjectMapper
        private EntityMapper entityMapper;

        @Override
        EntityMapper getMapper() {
            return entityMapper;
        }

        @Override
        @MyBatisMapperTest(testIsolation = false)
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisMapperTest(testIsolation = false)
    private static class MyBatisTestHybridItemNoClassLevelTestIsolation extends TestcaseItem {
        @InjectMapper
        private EntityMapper entityMapper;

        @Override
        EntityMapper getMapper() {
            return entityMapper;
        }

        @Override
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

    @MyBatisMapperTest
    private static class MyBatisTestNoMethodLevelTestIsolation extends TestcaseItem {
        @InjectMapper
        private EntityMapper entityMapper;

        @Override
        EntityMapper getMapper() {
            return entityMapper;
        }

        @Override
        @MyBatisMapperTest(testIsolation = false)
        public void exampleOfTestMethod() {
            // should not be executed
            Assertions.fail();
        }
    }

}
