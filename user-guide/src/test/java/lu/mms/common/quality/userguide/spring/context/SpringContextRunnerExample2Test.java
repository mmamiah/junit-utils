package lu.mms.common.quality.userguide.spring.context;

import lu.mms.common.quality.assets.spring.context.SpringContextRunner;
import lu.mms.common.quality.assets.spring.context.SpringContextRunnerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

/**
 * Test the ContextRunner instantiation.
 */
// tag::example[]
@ExtendWith({MockitoExtension.class, SpringContextRunnerExtension.class})
class SpringContextRunnerExample2Test {

    @SpringContextRunner(
        withMocks = EntityBrown.class
    )
    private ApplicationContextRunner appContextRunner;

    @Mock
    private EntityGreen entityGreenMock;

    private UserAccount userAccountMock;

    @BeforeEach
    void init() {
        assumeTrue(appContextRunner != null, "The <appContextRunner> cannot be null");
        assumeTrue(userAccountMock == null, "This mock should not be initialized");
        userAccountMock = mock(UserAccount.class);
    }

    @Test
    void shouldInjectMockDefinedInOutTestInTheApplicationContextWhenSimpleContext() {
        // Arrange
        assumeTrue(appContextRunner != null, "the ApplicationContext is not initialized");

        // Act
        appContextRunner.run(assertProvider -> {
            // Assert
            org.assertj.core.api.Assertions.assertThat(assertProvider).hasSingleBean(EntityGreen.class);
            org.assertj.core.api.Assertions.assertThat(assertProvider).hasSingleBean(UserAccount.class);
            org.assertj.core.api.Assertions.assertThat(assertProvider).hasSingleBean(EntityBrown.class);
            org.assertj.core.api.Assertions.assertThat(assertProvider).doesNotHaveBean(AnnotatedComponent.class);

            // The mock in the context should be the one we declared in the test
            final EntityGreen expectedEntityGreen = assertProvider.getBean(EntityGreen.class);
            assertThat(expectedEntityGreen, equalTo(entityGreenMock));
            final UserAccount expectedUserAccount = assertProvider.getBean(UserAccount.class);
            assertThat(expectedUserAccount, equalTo(userAccountMock));
            final EntityBrown expectedEntityBrown = assertProvider.getBean(EntityBrown.class);
            assertThat(MockUtil.isMock(expectedEntityBrown), equalTo(true));
        });
    }
}
// end::example[]
