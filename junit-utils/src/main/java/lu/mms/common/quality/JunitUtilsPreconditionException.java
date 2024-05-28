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

}
