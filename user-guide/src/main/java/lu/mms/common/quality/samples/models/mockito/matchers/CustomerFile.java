package lu.mms.common.quality.samples.models.mockito.matchers;

import java.io.File;
import java.nio.file.Path;

/**
 * The customer report.
 */
// tag::entity[]
public class CustomerFile {

    /**
     * Return the string representation of, if the file exists or not.
     * @param file The file
     * @return the "true"/"false" string.
     */
    public String readCustomerFile(final File file) {
        return Boolean.toString(file != null && file.exists());
    }

    /**
     * Return the string representation of, if the path exists or not.
     * @param path The path
     * @return the "true"/"false" string.
     */
    public String readCustomerPath(final Path path) {
        return Boolean.toString(path != null && path.toFile().exists());
    }
}
// end::entity[]
