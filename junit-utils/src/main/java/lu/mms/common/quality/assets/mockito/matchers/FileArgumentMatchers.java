package lu.mms.common.quality.assets.mockito.matchers;

import org.apiguardian.api.API;
import org.mockito.ArgumentMatcher;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * Custom argument matchers for {@link File} and {@link Path}.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public final class FileArgumentMatchers {

    private FileArgumentMatchers() {
        // hidden constructor
    }

    /**
     * Shortcut for argument matcher <b>any({@link File}.class)</b>.
     * @return File object
     */
    public static File anyFile() {
        return any(File.class);
    }

    /**
     * Customized {@link File} argument matcher. <br>
     * This method is really effective when used with Java 8 lambdas.
     * @param matcher {@link File} argument matcher
     * @return File object
     */
    public static File fileThat(final ArgumentMatcher<File> matcher) {
        return argThat(matcher);
    }

    /**
     * Shortcut for argument matcher <b>any({@link Path}.class)</b>.
     * @return Path object
     */
    public static Path anyPath() {
        return any(Path.class);
    }

    /**
     * Customized {@link Path} argument matcher. <br>
     * This method is really effective when used with Java 8 lambdas.
     * @param matcher {@link Path} argument matcher
     * @return Path object
     */
    public static Path pathThat(final ArgumentMatcher<Path> matcher) {
        return argThat(matcher);
    }

}
