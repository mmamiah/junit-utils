package lu.mms.common.quality.samples.assets.mockito.matchers;

import lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers;
import lu.mms.common.quality.samples.models.mockito.matchers.CustomerFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.anyFile;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.anyPath;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.pathThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Example showing how to use the {@link FileArgumentMatchers}.
 */
// tag::example[]
@ExtendWith(MockitoExtension.class)
class FileArgumentMatchersExample2Test {

    private static final List<String> MY_BOOLEANS = Arrays.asList("false", "true");

    @Mock
    private CustomerFile sut;

    @Test
    void shouldConfirmMockRespondsProperlyWhenAnyPathMatcherIsUsed() {
        // Arrange
        final String dummyPath = "dummyPath";
        final String anyPathExpectedAnswer = "anyFileAnswer";
        final String pathThatExpectedAnswer = "fileThatAnswer";

        when(sut.readCustomerPath(anyPath())).thenReturn(anyPathExpectedAnswer);
        when(sut.readCustomerPath(
            pathThat(path -> path.toString().equals(dummyPath)))
        ).thenReturn(pathThatExpectedAnswer);

        // Act
        final String expectationOne = sut.readCustomerPath(Path.of(""));
        assumeFalse(MY_BOOLEANS.contains(expectationOne));
        assumeTrue(expectationOne.equals(anyPathExpectedAnswer));

        final String expectationTwo = sut.readCustomerPath(Path.of(dummyPath));
        assumeFalse(MY_BOOLEANS.contains(expectationTwo));
        assumeTrue(expectationTwo.equals(pathThatExpectedAnswer));

        // Assert
        verify(sut, times(2)).readCustomerPath(anyPath());
        verify(sut).readCustomerPath(pathThat(p -> p.toString().equals(dummyPath)));
        verify(sut, never()).readCustomerFile(anyFile());
    }
}
// end::example[]
