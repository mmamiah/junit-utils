package lu.mms.common.quality;

/**
 * JunitUtils precondition violation exception.
 */
public class JunitUtilsPreconditionException extends RuntimeException {

    /**
     * @param message The message
     */
    public JunitUtilsPreconditionException(final String message) {
        super(message);
    }

    /**
     * @param message The message
     * @param throwable The error cause
     */
    public JunitUtilsPreconditionException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
