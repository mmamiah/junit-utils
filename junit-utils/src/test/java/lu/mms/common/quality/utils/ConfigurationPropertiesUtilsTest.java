package lu.mms.common.quality.utils;

import lu.mms.common.quality.platform.SpiConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test to ensure that the configuration properties are properly loaded. <br>
 * <b>impl note:</b> {@link ConfigurationPropertiesUtils#initJunitUtilsProperties()} is called by default at
 *          Framework start, no need to call it again.
 */
class ConfigurationPropertiesUtilsTest {

    @BeforeAll
    static void init() throws ClassNotFoundException {
        Class.forName(SpiConfiguration.class.getName());
    }

    @BeforeEach
    void ini() {
        assumeTrue(ConfigurationPropertiesUtils.properties != null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "junit-utils.log-reflections",           // defined in .YML
        "junit-utils.show-banner",               // defined in .YAML
        "junit-utils.component-scan",            // defined in .PROPERTIES
        "junit-utils.junit-platform-properties", // defined in 'junit-platform.properties'
        "junit-utils.junit-platform-yaml"       // defined in 'junit-platform.yaml'
    })
    void shouldFindConfigurationKeyWhenFullKeyDefined(final String propertyKey) {
        // Arrange
        final Properties properties = ConfigurationPropertiesUtils.properties;
        assumeTrue(properties.get(propertyKey) != null);

        // Act
        final Object value = ConfigurationFileType.retrieveConfiguration(properties, propertyKey);

        // Assert
        assertThat(value, notNullValue());
    }
}
