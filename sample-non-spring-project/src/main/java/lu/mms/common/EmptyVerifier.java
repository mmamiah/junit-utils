package lu.mms.common;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The Empty verifier.
 */
public class EmptyVerifier {

    public int getLength(final Object... args) {
        if (ArrayUtils.isEmpty(args)) {
            return 0;
        }
        return args.length;
    }
}
