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

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseNoDefinedSpringContextRunnerTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    private NoDefinedSpringContextRunner testCaseInstance;

    @BeforeEach
    private void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                NoDefinedSpringContextRunner.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldNotInitializeContextOnBeforeEachWhenContextNotAnnotated() {
        // Arrange
        assumeTrue(testCaseInstance != null);

        // Act
        sut.beforeEach(extensionContextMock);

        // Assert
        final Object contextRunner = testCaseInstance.getRunner();
        assertThat(contextRunner, nullValue());
    }

    @Test
    void shouldNotInitializeContextOnBeforeTestExecutionWhenContextNotAnnotated() {
        // Arrange
        assumeTrue(testCaseInstance != null);

        // Act
        sut.beforeTestExecution(extensionContextMock);

        // Assert
        final Object contextRunner = testCaseInstance.getRunner();
        assertThat(contextRunner, nullValue());
    }

    private static class NoDefinedSpringContextRunner {
        private ApplicationContextRunner runner;

        public ApplicationContextRunner getRunner() {
            return runner;
        }
    }
}
