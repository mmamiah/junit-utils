package lu.mms.common;

/**
 * Am Example of service.
 */
public class ArgumentsService {

    private EmptyVerifier verifier;

    public ArgumentsService() {
        // No arg constructor
    }

    public ArgumentsService(final EmptyVerifier verifier) {
        this.verifier = verifier;
    }

    public int countArgs(final Object[] args) {
        return verifier.getLength(args);
    }

    public EmptyVerifier getVerifier() {
        return verifier;
    }
}
