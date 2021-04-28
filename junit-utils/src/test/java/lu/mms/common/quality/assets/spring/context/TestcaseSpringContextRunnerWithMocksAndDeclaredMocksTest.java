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
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseSpringContextRunnerWithMocksAndDeclaredMocksTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    private SpringContextRunnerWithMocksAndDeclaredMocks testCaseInstance;

    @BeforeEach
    private void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                SpringContextRunnerWithMocksAndDeclaredMocks.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldSetMocksWhenContextWithMockAndTestcaseWithDeclaredMock() {
        // Arrange
        assumeTrue(testCaseInstance != null);
        sut.beforeEach(extensionContextMock);

        // Act
        sut.beforeTestExecution(extensionContextMock);

        // Assert
        final Object context = testCaseInstance.getRunner();
        assertThat(context, notNullValue());
        assertThat(context, instanceOf(ApplicationContextRunner.class));

        final ApplicationContextRunner simpleContext = (ApplicationContextRunner) context;
        simpleContext.run(contextAssertProvided -> {
            final TestcaseBean testcaseBean = contextAssertProvided.getBean(TestcaseBean.class);
            assertThat(MockUtil.isMock(testcaseBean), equalTo(true));
            assertThat(testcaseBean.getTheMock(), nullValue());
            assertThat(testcaseBean.getTheSpy(), nullValue());

            final ChildClassToMock theMock = contextAssertProvided.getBean(ChildClassToMock.class);
            assertThat(MockUtil.isMock(theMock), equalTo(true));

            final ChildClassToSpy theSpy = contextAssertProvided.getBean(ChildClassToSpy.class);
            assertThat(MockUtil.isSpy(theSpy), equalTo(true));
        });
    }

    private static class SpringContextRunnerWithMocksAndDeclaredMocks {
        @SpringContextRunner(
                withMocks = TestcaseBean.class
        )
        private ApplicationContextRunner runner;

        private final TestcaseBean testcaseBeanMock = mock(TestcaseBean.class);
        private final ChildClassToMock mockChild = mock(ChildClassToMock.class);
        private final ChildClassToSpy spyChild = spy(ChildClassToSpy.class);

        public ApplicationContextRunner getRunner() {
            return runner;
        }
    }

    // test case bean
    private static class TestcaseBean {
        private ChildClassToSpy theSpy;
        private ChildClassToMock theMock;

        public ChildClassToSpy getTheSpy() {
            return theSpy;
        }

        public ChildClassToMock getTheMock() {
            return theMock;
        }
    }

    // Class to be spied
    private static class ChildClassToSpy {
        private final String location = "me";

        ChildClassToSpy() {
        }

        String getLocation() {
            return location;
        }
    }

    // Class to be mock
    private static class ChildClassToMock {
        public int getAge() {
            return 15;
        }
    }
}
