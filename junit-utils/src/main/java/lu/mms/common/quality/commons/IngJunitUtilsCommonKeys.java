package lu.mms.common.quality.commons;

import java.util.Set;

/**
 * JUnit Utils keys.
 */
public final class IngJunitUtilsCommonKeys {

    /** The DOT key. */
    public static final String DOT = ".";
    /** The JUnit Platform key. */
    public static final String JUNIT_PLATFORM_KEY = "junit-platform";
    /** The JUnit Utils key. */
    public static final String JUNIT_UTILS_KEY = "junit-utils";
    /** The libraries config files handled by JUnit Utils. */
    public static final Set<String> LIBRARY_CONFIG_FILES_KEYS = Set.of(JUNIT_PLATFORM_KEY, JUNIT_UTILS_KEY);
    /** The JUnit Utils format. */
    public static final String JUNIT_UTILS_FORMAT = JUNIT_UTILS_KEY + ".%s";

    private IngJunitUtilsCommonKeys() {
        // hidden constructor
    }
}
