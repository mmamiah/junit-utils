package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.Environment;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseSpringContextRunnerWithPropertiesTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    private SpringContextRunnerWithProperties testCaseInstance;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                SpringContextRunnerWithProperties.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldSetPropertiesWhenSettingApplied() {
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
            final Environment env = contextAssertProvided.getBean(Environment.class);
            assertThat(env.getProperty("entity.colorName"), equalTo("brown"));
        });
    }

    private static class SpringContextRunnerWithProperties {
        @SpringContextRunner(
                withPropertyValues = "entity.colorName=brown"
        )
        private ApplicationContextRunner runner;

        public ApplicationContextRunner getRunner() {
            return runner;
        }
    }
}
