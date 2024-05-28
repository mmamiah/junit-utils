package lu.mms.common.quality.utils;

import lu.mms.common.quality.JunitUtilsPreconditionException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static lu.mms.common.quality.commons.IngJunitUtilsCommonKeys.JUNIT_UTILS_FORMAT;
import static lu.mms.common.quality.commons.IngJunitUtilsCommonKeys.JUNIT_UTILS_KEY;
import static lu.mms.common.quality.commons.IngJunitUtilsCommonKeys.LIBRARY_CONFIG_FILES_KEYS;

/**
 * Utility class to manage config properties files.
 */
public final class ConfigurationPropertiesUtils {

    static Properties properties;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPropertiesUtils.class);

    private static final String PACKAGE_SCAN_KEY = "component-scan";
    private static final String LOG_REFLECTIONS_KEY = "log-reflections";
    private static final String SHOW_BANNER_KEY = "show-banner";
    private static final String FANCY_BANNER_KEY = "fancy-banner";

    private static final String DUPLICATED_CONF_ENTRY_ERROR = "The property [%s] is duplicated, "
        + "having values [%s] and [%s].";

    private ConfigurationPropertiesUtils() {
        // hidden constructor
    }

    /**
     * Initializing the Framework context.
     */
    public static void initJunitUtilsProperties() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        properties = new Properties();
        Stream.of(ConfigurationFileType.values())
            .parallel()
            .flatMap(configType -> LIBRARY_CONFIG_FILES_KEYS.parallelStream()
                .flatMap(libraryName -> configurationFileToStream(classLoader, configType, libraryName))
            )
            .filter(entry -> entry.getKey().toString().startsWith("junit-utils"))
            // Not using '.collect<Collectors.toMap' in order access the entry key while detecting duplication
            .forEach(entry -> {
                final Object oldValue = properties.get(entry.getKey());
                if (oldValue != null) {
                    final String error = String.format(DUPLICATED_CONF_ENTRY_ERROR, entry.getKey(), oldValue,
                        entry.getValue());
                    throw new JunitUtilsPreconditionException(error);
                }
                properties.put(entry.getKey(), entry.getValue());
            });

        if (CollectionUtils.isEmpty(properties)) {
            LOGGER.warn("No [{}] configuration file found.", JUNIT_UTILS_KEY);
        } else {
            LOGGER.debug("Configuration file loaded: {}", properties);
        }
    }

    private static Stream<? extends Map.Entry<Object, Object>> configurationFileToStream(
                                                                final ClassLoader classLoader,
                                                                final ConfigurationFileType configType,
                                                                final String libraryName) {
        final URL resource = classLoader.getResource(configType.getFilename(libraryName));
        final Properties configs = configType.retrieveConfigurations(resource);
        return configs.entrySet().stream();
    }

    /**
     * Determine if the flag 'show banner' state.
     * @return true/false
     */
    public static synchronized boolean showBanner() {
        final String value = getJunitProperty(SHOW_BANNER_KEY);
        return Boolean.parseBoolean(value);
    }

    /**
     * Determine if the flag 'fancy banner' state.
     * @return true/false
     */
    public static synchronized boolean isFancyBanner() {
        final String value = getJunitProperty(FANCY_BANNER_KEY);
        return Boolean.parseBoolean(value);
    }

    /**
     * Determine is the flag for displaying 'Reflections' logs state.
     * @return true/false
     */
    public static synchronized boolean isLogReflections() {
        final String value = getJunitProperty(LOG_REFLECTIONS_KEY);
        return Boolean.parseBoolean(value);
    }

    public static synchronized String getPackageToScan() {
        return getJunitProperty(PACKAGE_SCAN_KEY);
    }

    private static synchronized String getJunitProperty(final String property) {
        Object prop = properties.get(property);
        if (prop == null) {
            prop = properties.get(String.format(JUNIT_UTILS_FORMAT, property));
        }
        return ObjectUtils.defaultIfNull(prop, StringUtils.EMPTY).toString();
    }

}
