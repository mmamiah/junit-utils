package lu.mms.common;

/**
 * Am Example of service.
 */
public class ArgumentsService {

    private EmptyVerifier verifier;

    /** Argument verifier. */
    public ArgumentsService() {
        // No arg constructor
    }

    /**
     * @param verifier The empty verifier
     */
    public ArgumentsService(final EmptyVerifier verifier) {
        this.verifier = verifier;
    }

    /**
     * @param args The arguments
     * @return  The arguments length
     */
    public int countArgs(final Object[] args) {
        return verifier.getLength(args);
    }

    public EmptyVerifier getVerifier() {
        return verifier;
    }
}
