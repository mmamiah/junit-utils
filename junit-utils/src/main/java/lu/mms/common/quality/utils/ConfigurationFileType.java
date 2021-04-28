package lu.mms.common.quality.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lu.mms.common.quality.commons.JunitUtilsCommonKeys.DOT;
import static lu.mms.common.quality.commons.JunitUtilsCommonKeys.JUNIT_UTILS_KEY;
import static lu.mms.common.quality.commons.JunitUtilsCommonKeys.LIBRARY_CONFIG_FILES_KEYS;

/**
 * The Configuration file types.
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

    private String fileExtension;
    private Function<URL, Map<Object, Object>> configExtractor;

    ConfigurationFileType(final String fileExtension,
                          final Function<URL, Map<Object, Object>> configExtractor) {
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
    public Map<Object, Object> retrieveConfigurations(final URL resource) {
        Map<Object, Object> properties = Collections.emptyMap();
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

    private static Map<Object, Object> readYaml(final URL resource) {
        Map<Object, Object> properties = new HashMap<>();
        try (InputStream inputStream = resource.openStream()) {
            if (inputStream != null) {
                properties = getProperties(new Yaml().load(inputStream));
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return properties;
    }

    private static Map<Object, Object> readProperties(final URL resource) {
        final Map<Object, Object> properties = new HashMap<>();
        try (InputStream inputStream = resource.openStream()) {
            final Properties tempProperties = new Properties();
            if (inputStream != null) {
                tempProperties.load(inputStream);
                extractLibraryProperties(properties, tempProperties);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return properties;
    }

    /**
     * Split the dotted property entry 'ont.two.three=four' in order to have a yaml like tree: <br>
     * <pre>
     * - one <br>
     *     |- two <br>
     *     |    |- three = four
     * </pre>
     * @param propertiesMap The properties maps
     * @param tempProperties the {@link Properties} to extract into the [propertiesMap]
     */
    private static void extractLibraryProperties(final Map<Object, Object> propertiesMap,
                                                 final Properties tempProperties) {
        tempProperties.entrySet().parallelStream()
            // Keep the property that starts with junit utils prefix
            .filter(entry -> entry.getKey().toString().startsWith(JUNIT_UTILS_KEY))
            .map(entry -> Pair.of(entry.getKey().toString().substring(JUNIT_UTILS_KEY.length() + 1), entry.getValue()))
            // turn the entry to a map with all child entries
            .flatMap(pair ->
                appendPropertyEntry(propertiesMap, pair.getKey(), pair.getValue())
                    .entrySet().parallelStream()
            )
            // put it all in the target map
            .forEach(entry -> propertiesMap.put(entry.getKey(), entry.getValue()));
    }

    private static Map<Object, Object> appendPropertyEntry(final Map<Object, Object> properties, final String entry,
                                                           final Object value) {
        String key = entry;
        if (entry.contains(DOT)) {
            key = StringUtils.substring(entry, 0, entry.indexOf(DOT));
        } else {
            return new HashMap<>(Map.of(key, value));
        }
        final Map<Object, Object> result = new HashMap<>();
        if (!CollectionUtils.isEmpty(properties)) {
            result.putAll(properties);
        }

        // build the selected property sub-entries
        final String subKey = StringUtils.substring(entry, entry.indexOf(DOT) + 1);
        final Map<Object, Object> entryMap = appendPropertyEntry(Map.of(), subKey, value);

        // add the selected property (new entry)
        final Object entryValue = result.get(key);
        if (entryValue == null) {
            properties.put(key, entryMap);
        } else if (entryValue instanceof Map) {
            final Map<Object, Object> values = (Map<Object, Object>) entryValue;
            values.putAll(entryMap);
        } else {
            throw new IllegalStateException(String.format("Property type not identified: [%s]", entryValue));
        }
        return result;
    }

    private static Map<Object, Object> getProperties(final Map<Object, Object> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            // when <properties> is empty return empty map (to avoid NPE). no more search needed.
            return Collections.emptyMap();
        }

        return LIBRARY_CONFIG_FILES_KEYS.parallelStream()
            // keeping only the non null properties
            .filter(ObjectUtils::allNotNull)
            // keeping entries that key starts with <junit-utils> or <junit-platform>
            .filter(filenameKey -> hasKeyStartingWith(properties, filenameKey))
            .flatMap(filenameKey -> {
                if (properties.get(filenameKey) instanceof Map) {
                    final Map<Object, Object> props = (Map) properties.get(filenameKey);
                    return props.entrySet().parallelStream();
                }
                // if there are some entries thats keys start with junit utils prefix, we pick it up
                final List<Map.Entry<Object, Object>> validEntries = properties.entrySet().parallelStream()
                    .filter(entry -> entry.getKey().toString().startsWith(filenameKey))
                    .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(validEntries)) {
                    return validEntries.parallelStream();
                }
                // otherwise we return a single entry set as stream
                return Set.of(Map.entry(filenameKey, properties.get(filenameKey))).parallelStream();
            })
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static boolean hasKeyStartingWith(final Map<Object, Object> properties, final String filenameKey) {
        return properties.keySet().parallelStream()
            .anyMatch(prop -> prop.toString().startsWith(filenameKey));
    }

    private static boolean isExtension(final URL url, final ConfigurationFileType configurationFileType) {
        return FilenameUtils.isExtension(url.getFile().toLowerCase(), configurationFileType.name().toLowerCase());
    }

}
