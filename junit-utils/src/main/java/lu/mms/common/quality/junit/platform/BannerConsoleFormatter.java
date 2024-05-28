package lu.mms.common.quality.junit.platform;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Banner console formatter.
 */
class BannerConsoleFormatter extends Formatter {

    /** ANSI reset color. */
    private static final String ANSI_RESET = "\u001B[0m";
    /** ANSI reset black. */
    private static final String ANSI_BLACK = "\033[0;30m";

    /** ANSI code for foreground color.*/
    private static final int FOREGROUND = 38;
    /** ANSI code for background color.*/
    private static final int BACKGROUND = 48;
    /** ANSI code for RGB color.*/
    private static final String ANSI_RENDITION_FORMAT = "\033[%s;2;%s;%s;%sm";
    /** ANSI code for orange foreground color.*/
    private static final String ORANGE_FOREGROUND = String.format(ANSI_RENDITION_FORMAT, FOREGROUND, 255, 98, 0);
    /** ANSI code for green foreground color.*/
    private static final String GREEN_FOREGROUND = String.format(ANSI_RENDITION_FORMAT, FOREGROUND, 36, 156, 95);
    /** ANSI code for red foreground color.*/
    private static final String RED_FOREGROUND = String.format(ANSI_RENDITION_FORMAT, FOREGROUND, 213, 80, 72);
    /** ANSI code for black foreground color.*/
    private static final String BLACK_FOREGROUND = String.format(ANSI_RENDITION_FORMAT, FOREGROUND, 51, 51, 51);

    private static final String MMS_KEY = "mms";
    private static final String DEFAULT_KEY = "default";
    private static final String JAY_KEY = "jay";
    private static final String UNIT_KEY = "unit";
    private static final String UTILS_KEY = "utils";
    private static final String CONTENT_KEY = "content";

    private Map<String, String> substitutionMap;

    /**
     * ConsoleLogFormatter default constructor.
     */
    public BannerConsoleFormatter() {
        substitutionMap = new HashMap<>();
        substitutionMap.put(MMS_KEY, ORANGE_FOREGROUND);
        substitutionMap.put(DEFAULT_KEY, ANSI_RESET);
        substitutionMap.put(JAY_KEY, BLACK_FOREGROUND);
        substitutionMap.put(UNIT_KEY, BLACK_FOREGROUND);
        substitutionMap.put(UTILS_KEY, BLACK_FOREGROUND);
        substitutionMap.put(CONTENT_KEY, ANSI_BLACK);
    }

    @Override
    public String format(final LogRecord record) {
        return new StringSubstitutor(substitutionMap).replace(record.getMessage());
    }

}
