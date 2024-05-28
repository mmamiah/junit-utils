package lu.mms.common.quality.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static lu.mms.common.quality.commons.IngJunitUtilsCommonKeys.DOT;
import static lu.mms.common.quality.commons.IngJunitUtilsCommonKeys.JUNIT_UTILS_KEY;

/**
 * The Configuration file types.
 * TODO: for yaml use ? YamlPropertiesFactoryBean ?
 */
enum ConfigurationFileType {

    /**
     * The properties file type.
     */
    PROPERTIES("properties", ConfigurationFileType::readProperties),

    /**
     * Yet Another Multicolumn Layout (1).
     */
    YML("yml", ConfigurationFileType::readYaml),

    /**
     * Yet Another Multicolumn Layout (2).
     */
    YAML("yaml", ConfigurationFileType::readYaml);

    private final String fileExtension;
    private final Function<URL, Properties> configExtractor;

    ConfigurationFileType(final String fileExtension,
                          final Function<URL, Properties> configExtractor) {
        this.fileExtension = fileExtension;
        this.configExtractor = configExtractor;
    }

    public String getFilename(final String baseFilename) {
        return String.format("%s.%s", baseFilename, fileExtension);
    }

    public boolean isCandidate(final URL resource) {
        boolean result = false;
        if (resource != null) {
            if (isYaml()) {
                result = isExtension(resource, YAML) || isExtension(resource, YML);
            } else {
                result = isExtension(resource, this);
            }
        }
        return result;
    }

    public boolean isYaml() {
        return this == ConfigurationFileType.YAML || this == ConfigurationFileType.YML;
    }

    /**
     * Extract properties from configuration file.
     * @param resource The resource
     * @return  the properties maps
     */
    public Properties retrieveConfigurations(final URL resource) {
        Properties properties = new Properties();
        if (isCandidate(resource)) {
            try {
                properties = configExtractor.apply(resource);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return properties;
    }

    /**
     * Extract property from configuration map. <br>
     * This method will navigate through the given map and retun the first matching key.
     * @param configs The configurations to search
     * @param configKey The configuration key to search
     * @return  the properties maps
     */
    public static Object retrieveConfiguration(final Object configs, final String configKey) {
        if (!(configs instanceof Map)) {
            return configs;
        }
        final Map<Object, Object> configsMap = (Map<Object, Object>) configs;
        // if key if found at the root we just return it
        if (configsMap.containsKey(configKey)) {
            return configsMap.get(configKey);
        }
        if (CollectionUtils.isEmpty(configsMap) || StringUtils.isBlank(configKey)) {
            return null;
        }

        // remove the lead LIB prefix if relevant
        final String key;
        if (configKey.contains(JUNIT_UTILS_KEY)) {
            key = configKey.substring(JUNIT_UTILS_KEY.length() + 1);
        } else {
            key = configKey;
        }

        final String keyItem;
        // if the key contains a '.' pick up the prefix and ...
        if (key.contains(DOT)) {
            keyItem = key.substring(0, key.indexOf(DOT));
        } else {
            keyItem = key;
        }

        // ... check recursively the children entries.
        return retrieveConfiguration(configsMap.get(keyItem), key.substring(key.indexOf(DOT) + 1));
    }

    private static Properties readYaml(final URL resource) {
        final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new FileUrlResource(resource));
        return factory.getObject();
    }

    private static Properties readProperties(final URL resource) {
        try (InputStream inputStream = resource.openStream()) {
            final Properties tempProperties = new Properties();
            if (inputStream != null) {
                tempProperties.load(inputStream);
                return tempProperties;
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return new Properties();
    }

    private static boolean isExtension(final URL url, final ConfigurationFileType configurationFileType) {
        return FilenameUtils.isExtension(url.getFile().toLowerCase(), configurationFileType.name().toLowerCase());
    }

}
