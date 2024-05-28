package lu.mms.common.quality.userguide.spring.context;

import lu.mms.common.quality.assets.spring.context.SpringContextRunner;
import lu.mms.common.quality.assets.spring.context.SpringContextRunnerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test the ContextRunner instantiation.
 */
// tag::example[]
@ExtendWith({MockitoExtension.class, SpringContextRunnerExtension.class})
class SpringContextRunnerExample1Test {

    @SpringContextRunner(
        withMocks = UserAccount.class,
        withUserConfiguration = ConfigUser.class
    )
    private ApplicationContextRunner appContextRunner;

    @BeforeEach
    void init() {
        assumeTrue(appContextRunner != null, "The <appContextRunner> cannot be null");
    }

    @Test
    void shouldInjectMockDefinedInOutTestInTheApplicationContextWhenSimpleContext() {
        // Arrange
        assumeTrue(appContextRunner != null, "the ApplicationContext is not initialized");

        // Act
        appContextRunner.run(assertProvider -> {
            // Assert
            assertThat(assertProvider).doesNotHaveBean(AnnotatedComponent.class);

            // This entity is declared in the User configuration
            assertThat(assertProvider).hasSingleBean(EntityBrown.class);
        });
    }
}
// end::example[]
