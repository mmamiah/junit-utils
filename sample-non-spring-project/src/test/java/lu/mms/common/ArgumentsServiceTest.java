package lu.mms.common;

import lu.mms.common.quality.assets.unittest.UnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * With this test we check that Mockito, Hamcrest and the custom extension works.
 */
@UnitTest
class ArgumentsServiceTest {

    @InjectMocks
    private ArgumentsService sut;

    @Mock
    private EmptyVerifier verifierMock;

    @Test
    void shouldConfirmSutIsProperlyInitialized() {
        // Arrange
        assumeTrue(sut != null);

        // Act
        final EmptyVerifier result = sut.getVerifier();

        // Assert
        assertThat(result, notNullValue());
        assertThat(result, equalTo(verifierMock));
    }

    @Test
    void shouldConfirmArgumentCount() {
        // Arrange
        final Object[] args = null;

        // Act
        final int result = sut.countArgs(args);

        // Assert
        assertThat(result, equalTo(0));
    }
}
