package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestcaseSpringContextRunnerWithMapperPackageTest {

    private final SpringContextRunnerExtension sut = new SpringContextRunnerExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    private SpringContextRunnerWithMapperPackage testCaseInstance;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        testCaseInstance = TestcaseHelper.newHelper(extensionContextMock).prepareTestCaseMock(
                SpringContextRunnerWithMapperPackage.class,
                testInfo.getTestMethod().orElse(null)
        );
    }

    @Test
    void shouldSetMappersWhenSettingApplied() {
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
            assertThat(contextAssertProvided.containsBean("testcaseMapper"), equalTo(true));

            final TestcaseMapper testcaseMapper = contextAssertProvided.getBean(TestcaseMapper.class);
            assertThat(testcaseMapper, notNullValue());
            assertThat(MockUtil.isMock(testcaseMapper), equalTo(true));
        });
    }

    private static class SpringContextRunnerWithMapperPackage {
        @SpringContextRunner(
                mappersPackage = "lu.mms.common.quality.assets.spring.context"
        )
        private ApplicationContextRunner runner;

        public ApplicationContextRunner getRunner() {
            return runner;
        }
    }

}
