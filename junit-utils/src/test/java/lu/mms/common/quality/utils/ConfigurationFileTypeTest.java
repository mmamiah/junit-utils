package lu.mms.common.quality.utils;

import lu.mms.common.quality.junit.platform.SpiConfiguration;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test the function provided by {@link ConfigurationFileType}.
 */
class ConfigurationFileTypeTest {

    private static final String FILENAME_TEMPLATE = "%s.%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpiConfiguration.class);

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldReturnEmptyMapWhenResourceIsNull(final ConfigurationFileType configType) {
        // Act
        final Map<Object, Object> properties = configType.retrieveConfigurations(null);

        // Assert
        assertThat(properties, notNullValue());
        assertThat(properties, anEmptyMap());
    }

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldNotFindConfigWhenFileIsMissing(final ConfigurationFileType configType) throws MalformedURLException {
        // Arrange
        // prepared files object (not creating real files)
        final List<File> configFiles = Arrays.asList(
            new File("helloOne.properties"),
            new File("helloTwo.yaml"),
            new File("helloTree.yml")
        );

        configFiles.parallelStream()
            // map the files to URL
            .map(configFile -> {
                URL url = null;
                try {
                    url = configFile.toURI().toURL();
                } catch (IOException exception) {
                    LOGGER.error("Failed to resolve the url for [{}].", configFile, exception);
                }
                return Optional.ofNullable(url);
            })
            .filter(Optional::isEmpty)
            .forEach(url -> {
                // Act
                final Map<Object, Object> properties = configType.retrieveConfigurations(url.get());

                // Assert
                assertThat(properties, notNullValue());
                assertThat(properties, anEmptyMap());
            });
    }

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldFindNothingWhenMissingPropertyInGivenFile(final ConfigurationFileType fileFormat) throws IOException {
        // Arrange
        final String filename = fileFormat.getFilename("testcase");
        final String notExistingProperty = "customer.address";
        // create an empty file
        final File configFile = tempDir.resolve(filename).toFile();
        assumeTrue(configFile.createNewFile());

        final URL fileUrl = configFile.toURI().toURL();

        // loop into [ConfigurationFileType] and try to retrieve the property key
        Stream.of(ConfigurationFileType.values()).forEach(type -> {
            // Act
            final Map<Object, Object> config = type.retrieveConfigurations(fileUrl);

            // Assert
            assertThat(config, notNullValue());
            assertThat(config.get(notExistingProperty), nullValue());
            assertThat(config, anEmptyMap());
        });
    }

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldNotConsiderPropertyWhenNotAnIngJunitUtilsProperty(final ConfigurationFileType fileFormat)
                                                                throws IOException {
        // Arrange
        final String filename = fileFormat.getFilename("testcase");
        final String propertyKey = "customer.country"; // property not starting by [junit-utils]
        final String propertyValue = "Luxembourg";
        final String propertyEntry = samplePropertyEntryProvider(fileFormat, propertyKey, propertyValue);
        // create the file with data
        final URL fileUrl = createFile(tempDir, filename, propertyEntry).toUri().toURL();

        // loop into [ConfigurationFileType] and try to retrieve the property key
        Stream.of(ConfigurationFileType.values()).forEach(type -> {
            // Act
            final Map<Object, Object> config = type.retrieveConfigurations(fileUrl);

            // Assert
            assertThat(config, notNullValue());
            assertThat(config, anEmptyMap());
        });
    }

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldConsiderPropertyWhenItIsAnIngJunitUtilsProperty(final ConfigurationFileType fileFormat)
                                                                throws IOException {
        // Arrange
        final String filename = fileFormat.getFilename("testcase");
        final String propertyKey = "junit-utils.customer.country"; // property starting by [junit-utils]
        final String propertyValue = "Luxembourg";
        final String propertyEntry = samplePropertyEntryProvider(fileFormat, propertyKey, propertyValue);
        // create the file with data
        final URL fileUrl = createFile(tempDir, filename, propertyEntry).toUri().toURL();

        // Act
        final Map<Object, Object> config = fileFormat.retrieveConfigurations(fileUrl);

        // Assert
        assertThat(config, aMapWithSize(1));
        assertThat(config.values(), notNullValue());

        // loop into [ConfigurationFileType] and try to retrieve the property key
        Stream.of(ConfigurationFileType.values())
            .filter(type -> fileFormat != type && (fileFormat.isYaml() != type.isYaml()))
            .forEach(type -> {
                final Map<Object, Object> typeConfig = type.retrieveConfigurations(fileUrl);
                assertThat(typeConfig, notNullValue());
                assertThat(typeConfig, anEmptyMap());
            });
    }

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldFindTheValueInPropertiesMap(final ConfigurationFileType fileFormat) throws IOException {
        // Arrange
        final String filename = fileFormat.getFilename("testcase");
        final String propertyKey = "junit-utils.customer.country"; // property starting by [junit-utils]
        final String expectedPropertyValue = "Luxembourg";
        final String propertyEntry = samplePropertyEntryProvider(fileFormat, propertyKey, expectedPropertyValue);
        // create the file with data
        final URL fileUrl = createFile(tempDir, filename, propertyEntry).toUri().toURL();

        // retrieve configuration map
        final Map<Object, Object> configs = fileFormat.retrieveConfigurations(fileUrl);

        // Act
        final Object actualPropertyValue = ConfigurationFileType.retrieveConfiguration(configs, propertyKey);

        // Assert
        assertThat(actualPropertyValue, notNullValue());
        assertThat(actualPropertyValue.toString(), equalTo(expectedPropertyValue));
    }

    @ParameterizedTest
    @EnumSource(ConfigurationFileType.class)
    void shouldComputeFileName(final ConfigurationFileType configType) {
        // Arrange
        final String baseFilename = "my_lovely_file";

        // Act
        final String filename = configType.getFilename(baseFilename);

        // Assert
        assertThat(filename, notNullValue());
        assertThat(filename, allOf(
            containsStringIgnoringCase(baseFilename),
            containsStringIgnoringCase(configType.name())
        ));

        final String expectedFileName = String.format(FILENAME_TEMPLATE, baseFilename, configType.name().toLowerCase());
        assertThat(filename, equalTo(expectedFileName));
    }

    private static Path createFile(final Path parentFolder, final String fileName, final String... data) {
        final List<String> lines = Arrays.asList(data);
        final Path file = parentFolder.resolve(fileName);
        Path result = null;
        try {
            result = Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.error("Failed to create the file [{}]", file);
        } finally {
            assert result != null : "The file was not created.";
        }
        return result;
    }

    private static String samplePropertyEntryProvider(final ConfigurationFileType fileFormat, final String propertyKey,
                                                      final String propertyValue) {
        String separator = "%s= %s";
        if (fileFormat.isYaml()) {
            separator = "%s: %s";
        }
        return String.format(separator, propertyKey, propertyValue);
    }
}
