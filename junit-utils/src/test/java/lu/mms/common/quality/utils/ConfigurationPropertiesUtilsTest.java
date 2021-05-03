package lu.mms.common.quality.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test to ensure that the configuration properties are properly loaded. <br>
 * <b>impl note:</b> {@link ConfigurationPropertiesUtils#initJunitUtilsProperties()} is called by default at
 *          Framework start, no need to call it again.
 */
class ConfigurationPropertiesUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "log-reflections",          // defined in .YML
        "show-banner",              // defined in .YAML
        "component-scan",           // defined in .PROPERTIES
        "junit-platform-properties", // defined in 'junit-platform.properties'
        "junit-platform-yaml",      // defined in 'junit-platform.yaml'
        "junit-platform-yml"        // defined in 'junit-platform.properties'
    })
    void shouldFindYamlConfigurationWhenEntryKeyProvided(final String propertyKey) {
        // Arrange

        // Act
        final Object simpleValue = ConfigurationPropertiesUtils.properties.get(propertyKey);

        // Assert
        assertThat(simpleValue, notNullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "junit-utils.log-reflections",           // defined in .YML
        "junit-utils.show-banner",               // defined in .YAML
        "junit-utils.component-scan",            // defined in .PROPERTIES
        "junit-utils.junit-platform-properties", // defined in 'junit-platform.properties'
        "junit-utils.junit-platform-yaml",       // defined in 'junit-platform.yaml'
        "junit-utils.junit-platform-yml"         // defined in 'junit-platform.properties'
    })
    void shouldFindConfigurationKeyWhenFullKeyDefined(final String propertyKey) {
        // Arrange
        final Map<Object, Object> properties = ConfigurationPropertiesUtils.properties;
        // should not find the key as we do not store full keys in the map
        assumeTrue(properties.get(propertyKey) == null);

        // Act
        final Object value = ConfigurationFileType.retrieveConfiguration(properties, propertyKey);

        // Assert
        assertThat(value, notNullValue());
    }
}
