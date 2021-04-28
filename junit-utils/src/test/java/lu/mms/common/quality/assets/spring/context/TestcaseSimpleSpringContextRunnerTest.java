package lu.mms.common.quality.assets.spring.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseSimpleSpringContextRunnerTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    private SimpleSpringContextRunner testCaseInstance;

    @BeforeEach
    private void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                SimpleSpringContextRunner.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldInitializeContextOnBeforeEachWhenFieldProperlyAnnotated() {
        // Arrange
        assumeTrue(testCaseInstance != null);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final Object contextRunner = testCaseInstance.getRunner();
        assertThat(contextRunner, notNullValue());
        assertThat(contextRunner, instanceOf(ApplicationContextRunner.class));
    }

    @Test
    void shouldInitializeContextOnBeforeTestExecutionWhenFieldProperlyAnnotated() {
        // Arrange
        assumeTrue(testCaseInstance != null);
        sut.beforeEach(extensionContextMock);

        // Act
        sut.beforeTestExecution(extensionContextMock);

        // Assert
        final Object contextRunner = testCaseInstance.getRunner();
        assertThat(contextRunner, notNullValue());
        assertThat(contextRunner, instanceOf(ApplicationContextRunner.class));
    }

    /**
     * BUGFIX #13: SpringContextRunner throws a BeanDefinitionStoreException
     */
    @Test
    void shouldEnsureBeanOverridingIsSetWhenCreatingInitializingNewContextAndBugfix13() {
        // Arrange
        assumeTrue(testCaseInstance != null);
        sut.beforeEach(extensionContextMock);

        // Act
        sut.beforeTestExecution(extensionContextMock);

        // Assert
        final Object contextRunner = testCaseInstance.getRunner();
        assertThat(contextRunner, notNullValue());
        assertThat(contextRunner, instanceOf(ApplicationContextRunner.class));

        final Boolean canOverride = (Boolean) ReflectionTestUtils.getField(
                contextRunner,
                "allowBeanDefinitionOverriding"
        );
        // BUGFIX #13
        assertThat(canOverride, equalTo(true));
    }

    private static class SimpleSpringContextRunner {
        @SpringContextRunner
        private ApplicationContextRunner runner;

        public ApplicationContextRunner getRunner() {
            return runner;
        }
    }
}
