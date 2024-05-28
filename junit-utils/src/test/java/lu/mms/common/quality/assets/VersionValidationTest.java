package lu.mms.common.quality.assets;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import static lu.mms.common.quality.assets.JunitUtilsExtension.isValidVersion;

class VersionValidationTest {

    @Test
    void shouldValidateVersion() {
        // Arrange

        // Act
        boolean result = isValidVersion("3.1.0");

        // Assert
        MatcherAssert.assertThat(result, Is.is(true));
    }

}