package lu.mms.common.quality.samples.assets.spring.context;

import lu.mms.common.quality.assets.spring.context.SpringContextRunner;
import lu.mms.common.quality.assets.spring.context.SpringContextRunnerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

/**
 * Test the ContextRunner instantiation.
 */
@ExtendWith({MockitoExtension.class, SpringContextRunnerExtension.class})
class SpringContextRunnerExtensionTest {

    @SpringContextRunner
    private ApplicationContextRunner simpleContext;

    @SpringContextRunner(
        withConfiguration = ConfigSpringBoot.class
    )
    private ApplicationContextRunner contextWithConfiguration;

    @SpringContextRunner(
        withPropertyValues = "entity.colorName=brown",
        withUserConfiguration = ConfigUser.class
    )
    private ApplicationContextRunner contextWithUserConfig;

    @SpringContextRunner(
        withUserConfiguration = ConfigUser.class,
        withConfiguration = ConfigSpringBoot.class
    )
    private ApplicationContextRunner contextWithBothConfig;

    @Mock
    private EntityGreen entityGreenMock;

    private EntityBlue entityBlueMock;

    @BeforeEach
    void init() {
        assumeTrue(simpleContext != null, "The <simpleContext> cannot be null");
        assumeTrue(contextWithConfiguration != null, "The <contextWithConfiguration> cannot be null");
        assumeTrue(contextWithUserConfig != null, "The <contextWithUserConfig> cannot be null");
        assumeTrue(contextWithBothConfig != null, "The <contextWithBothConfig> cannot be null");

        assumeTrue(entityBlueMock == null, "This mock should not be initialized");
        entityBlueMock = mock(EntityBlue.class);
    }

    @Test
    void shouldInjectMockDefinedInOutTestInTheApplicationContextWhenSimpleContext() {
        // Arrange
        assumeTrue(simpleContext != null, "the ApplicationContext is not initialized");

        // Act
        simpleContext.run(contextAssertProvided -> {
            // Assert
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityGreen.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBlue.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).doesNotHaveBean(AnnotatedComponent.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).doesNotHaveBean(EntityBrown.class);

            // The mock in the context should be the one we declared in the test
            final EntityGreen expectedEntityGreen = contextAssertProvided.getBean(EntityGreen.class);
            assertThat(expectedEntityGreen, equalTo(entityGreenMock));

            final EntityBlue expectedEntityBlue = contextAssertProvided.getBean(EntityBlue.class);
            assertThat(expectedEntityBlue, equalTo(entityBlueMock));
        });
    }

    @Test
    void shouldInjectBeanCoveredByConfiguration() {
        // Arrange
        assumeTrue(contextWithConfiguration != null, "the ApplicationContext is not initialized");

        // Act
        contextWithConfiguration.run(contextAssertProvided -> {
            // Assert
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityGreen.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBlue.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(AnnotatedComponent.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBrown.class);

            // The mock in the context should be the one we declared in the test
            final EntityGreen expectedEntityGreen = contextAssertProvided.getBean(EntityGreen.class);
            assertThat(expectedEntityGreen, equalTo(entityGreenMock));

            final EntityBlue expectedEntityBlue = contextAssertProvided.getBean(EntityBlue.class);
            assertThat(expectedEntityBlue, equalTo(entityBlueMock));
        });
    }

    @Test
    void shouldInjectBeanDefinedInUserConfiguration() {
        // Arrange
        assumeTrue(contextWithUserConfig != null, "the ApplicationContext is not initialized");

        // Act
        contextWithUserConfig.run(contextAssertProvided -> {
            // Assert
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityGreen.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBlue.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).doesNotHaveBean(AnnotatedComponent.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBrown.class);

            // The mock in the context should be the one we declared in the test
            final EntityGreen expectedEntityGreen = contextAssertProvided.getBean(EntityGreen.class);
            assertThat(expectedEntityGreen, equalTo(entityGreenMock));

            final EntityBlue expectedEntityBlue = contextAssertProvided.getBean(EntityBlue.class);
            assertThat(expectedEntityBlue, equalTo(entityBlueMock));

            final EntityBrown expectedEntityBrown = contextAssertProvided.getBean(EntityBrown.class);
            assertThat(expectedEntityBrown.getName(), equalTo("brown"));
        });
    }

    @Test
    void shouldInjectBeanDefinedBothConfiguration() {
        // Arrange
        assumeTrue(contextWithBothConfig != null, "the ApplicationContext is not initialized");

        // Act
        contextWithBothConfig.run(contextAssertProvided -> {
            // Assert
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityGreen.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBlue.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(AnnotatedComponent.class);
            org.assertj.core.api.Assertions.assertThat(contextAssertProvided).hasSingleBean(EntityBrown.class);

            // The mock in the context should be the one we declared in the test
            final EntityGreen expectedEntityGreen = contextAssertProvided.getBean(EntityGreen.class);
            assertThat(expectedEntityGreen, equalTo(entityGreenMock));
            // entity.color.name
            final EntityBlue expectedEntityBlue = contextAssertProvided.getBean(EntityBlue.class);
            assertThat(expectedEntityBlue, equalTo(entityBlueMock));
        });
    }

}
