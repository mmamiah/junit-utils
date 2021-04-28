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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseSpringContextRunnerWithUserConfigurationTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    private SpringContextRunnerWithUserConfiguration testCaseInstance;

    @BeforeEach
    private void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                SpringContextRunnerWithUserConfiguration.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldSetUserConfigWhenSettingApplied() {
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
            assertThat(contextAssertProvided.containsBean("testcaseBean"), equalTo(true));
            final TestcaseBean testcaseBean = contextAssertProvided.getBean(TestcaseBean.class);
            assertThat(MockUtil.isMock(testcaseBean), equalTo(true));
        });
    }

    private static class SpringContextRunnerWithUserConfiguration {
        @SpringContextRunner(
                withUserConfiguration = TestcaseConfiguration.class
        )
        private ApplicationContextRunner runner;

        public ApplicationContextRunner getRunner() {
            return runner;
        }
    }

    @Configuration
    private static class TestcaseConfiguration {
        TestcaseConfiguration() {
            // forcing public default constructor because it wasn't visible during context instantiation.
        }

        // test case config
        @Bean
        public TestcaseBean testcaseBean() {
            return mock(TestcaseBean.class);
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
        private String location = "me";

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
