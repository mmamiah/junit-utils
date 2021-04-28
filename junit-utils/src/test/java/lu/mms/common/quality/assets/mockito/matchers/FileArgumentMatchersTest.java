package lu.mms.common.quality.assets.mockito.matchers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.anyFile;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.anyPath;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.fileThat;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.pathThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Testing the mockito matcher {@link FileArgumentMatchers#anyFile()}.
 */
@ExtendWith(MockitoExtension.class)
class FileArgumentMatchersTest {

    private static final String ANY = "any";

    private static final Path ANY_PATH = Path.of(ANY);

    @Mock
    private Customer customerMock;

    @Test
    void shouldNotVerifyAnyFileWhenFileIsNull() {
        // Act
        customerMock.handleFile(null);

        // Assert
        verify(customerMock, never()).handleFile(anyFile());
    }

    @Test
    void shouldConfirmAnyFileWhenFileProvided() {
        // Act
        customerMock.handleFile(ANY_PATH.toFile());

        // Assert
        verify(customerMock).handleFile(anyFile());
    }

    @Test
    void shouldConfirmAFileThatWhenFileProvided() {
        // Act
        customerMock.handleFile(ANY_PATH.toFile());

        // Assert
        verify(customerMock).handleFile(fileThat(file -> file.getName().equals(ANY)));
    }

    @Test
    void shouldNotVerifyAnyPathWhenPathIsNull() {
        // Act
        customerMock.handlePath(null);

        // Assert
        verify(customerMock, never()).handlePath(anyPath());
    }

    @Test
    void shouldConfirmAnyPathWhenPathProvided() {
        // Act
        customerMock.handlePath(ANY_PATH);

        // Assert
        verify(customerMock).handlePath(anyPath());
    }

    @Test
    void shouldConfirmAPathThatWhenPathProvided() {
        // Act
        customerMock.handlePath(ANY_PATH);

        // Assert
        verify(customerMock).handlePath(pathThat(path -> path.getFileName().toString().equals(ANY)));
    }

    private static class Customer {
        void handleFile(final File myFile) {
            // empty method
        }

        void handlePath(final Path myPath) {
            // empty method
        }
    }

}
