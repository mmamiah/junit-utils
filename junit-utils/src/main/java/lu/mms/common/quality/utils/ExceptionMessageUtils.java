package lu.mms.common.quality.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * utility class to manage exception message.
 */
public final class ExceptionMessageUtils {

    private ExceptionMessageUtils() {
        // Hidden constructor
    }

    /**
     * Extract the error message if exists. <br>
     * if not, check if the cause is different thant the actual exception and call this method recursively with cause
     * exception as arguments. <br>
     * if not, return the class simple name as error message.
     * @param exception The exception containing the error message
     * @return  The error message string
     */
    public static String extractErrorMessage(final Throwable exception) {
        if (StringUtils.isNotBlank(exception.getMessage())) {
            return exception.getMessage();
        }

        if (exception.getCause() != null && exception.getCause() != exception) {
            return extractErrorMessage(exception.getCause());
        }

        return exception.getClass().getSimpleName();
    }
}
