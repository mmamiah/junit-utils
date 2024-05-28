package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.JunitUtilsPreconditionException;
import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseNotAssignableTypeTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    private NotAssignableType testCaseInstance;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);
        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                NotAssignableType.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldThrowExceptionOnBeforeEachWhenNotAssignableType() {
        // Arrange
        assumeTrue(testCaseInstance != null);

        // Act
        final Exception exception = assertThrows(Exception.class, () -> sut.beforeEach(extensionContextMock));

        // Assert
        assertThat(exception, instanceOf(JunitUtilsPreconditionException.class));
    }

    @Test
    void shouldThrowExceptionOnBeforeTestExecutionWhenNotAssignableType() {
        // Arrange
        assumeTrue(testCaseInstance != null);

        // Act
        final Exception exception = assertThrows(Exception.class, () -> sut.beforeTestExecution(extensionContextMock));

        // Assert
        assertThat(exception, instanceOf(JunitUtilsPreconditionException.class));
    }

    private static class NotAssignableType {
        @SpringContextRunner
        private String simpleContext;
    }
}
