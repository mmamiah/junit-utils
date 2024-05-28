package lu.mms.common.quality.assets.spring.context;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith({MockitoExtension.class, SpringContextRunnerExtension.class})
class SpringContextRunnerPropertySourceTest {

    @SpringContextRunner(
            withPropertySource = @PropertySource({"junit-platform.yaml", "junit-utils.properties"})
    )
    private ApplicationContextRunner appContextRunner;

    @SpringContextRunner(
            withPropertySource = @PropertySource({"application-test.yml", "application.yml"})
    )
    private ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void init() {
        assumeTrue(appContextRunner != null, "The <appContextRunner> cannot be null");
    }

    @Test
    void shouldReadYamlAndPropertiesFiles() {
        // Arrange
        assumeTrue(appContextRunner != null, "the ApplicationContext is not initialized");

        // Act
        appContextRunner.run(assertProvider -> {
            // Assert
            final Environment environment = assertProvider.getBean(Environment.class);
            MatcherAssert.assertThat(environment.getProperty("junit-utils.indice-yaml"), equalTo("sad4s"));
            MatcherAssert.assertThat(environment.getProperty("junit-utils.component-scan"), equalTo("lu.mms"));
        });
    }
    @Test
    void shouldOverrideApplicationProperties() {
        // Arrange
        assumeTrue(applicationContextRunner != null, "the ApplicationContext is not initialized");

        // Act
        applicationContextRunner.run(assertProvider -> {
            // Assert
            final Environment environment = assertProvider.getBean(Environment.class);
            MatcherAssert.assertThat(environment.getProperty("customer.name"), equalTo("Kramer"));
            MatcherAssert.assertThat(environment.getProperty("customer.surname"), equalTo("Claudia"));
        });
    }

}