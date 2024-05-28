package lu.mms.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test that the default library and behavior works out of non-spring app.
 * For instance, the Hamcrest Lib is used here.
 */
class EmptyVerifierTest {

    private final EmptyVerifier sut = new EmptyVerifier();

    @Test
    void shouldConfirmTheIsNoArgs() {
        // Arrange

        // Act
        final int result = sut.getLength();

        // Assert
        assertThat(result, equalTo(0));
    }

    @Test
    void shouldConfirmInputHasSingleArg() {
        // Arrange
        final Object[] args = {2021};

        // Act
        final int result = sut.getLength(args);

        // Assert
        assertThat(result, equalTo(1));
    }

}
