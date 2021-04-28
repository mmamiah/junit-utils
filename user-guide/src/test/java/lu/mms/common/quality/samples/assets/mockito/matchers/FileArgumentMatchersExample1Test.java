package lu.mms.common.quality.samples.assets.mockito.matchers;

import lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers;
import lu.mms.common.quality.samples.models.mockito.matchers.CustomerFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.anyFile;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.anyPath;
import static lu.mms.common.quality.assets.mockito.matchers.FileArgumentMatchers.fileThat;
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
class FileArgumentMatchersExample1Test {

    private static final List<String> MY_BOOLEANS = Arrays.asList("false", "true");

    @Mock
    private CustomerFile sut;

    @Test
    void shouldConfirmMockRespondsProperlyWhenAnyFileMatcherIsUsed() {
        // Arrange
        final String filename = "dummyFile.txt";
        final String anyFileExpectedAnswer = "anyFileAnswer";
        final String fileThatExpectedAnswer = "fileThatAnswer";

        when(sut.readCustomerFile(anyFile())).thenReturn(anyFileExpectedAnswer);

        when(sut.readCustomerFile(
            fileThat(file -> file.getName().equals(filename)))
        ).thenReturn(fileThatExpectedAnswer);

        // Act
        final String expectationOne = sut.readCustomerFile(new File("any"));
        assumeFalse(MY_BOOLEANS.contains(expectationOne));
        assumeTrue(expectationOne.equals(anyFileExpectedAnswer));

        final String expectationTwo = sut.readCustomerFile(new File(filename));
        assumeFalse(MY_BOOLEANS.contains(expectationTwo));
        assumeTrue(expectationTwo.equals(fileThatExpectedAnswer));

        // Assert
        verify(sut, times(2)).readCustomerFile(anyFile());
        verify(sut).readCustomerFile(fileThat(f -> f.getName().equals(filename)));
        verify(sut, never()).readCustomerPath(anyPath());
    }
}
// end::example[]
