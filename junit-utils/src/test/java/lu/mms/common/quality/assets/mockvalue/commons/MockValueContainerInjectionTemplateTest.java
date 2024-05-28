package lu.mms.common.quality.assets.mockvalue.commons;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Unit test to validate the MockValue injection template.
 */
class MockValueContainerInjectionTemplateTest {

    private MockValueContainerInjectionTemplate sut;

    // in seconds: This will cause all files to be considered as <not old>, and therefore, will not be handled
    @MockValue("${file-age-in-sec:1800}")
    private final int defaultFileAge = 180;

    @MockValue(
            value = "${file-age-in-sec:1800}",
            testcase = {
                    "shouldDefaultValueWhenDuplicatedSettings"
            }
    )
    private int fileAge = 2;

    @Sample
    private UserFolder userFolder;

    @BeforeEach
    void init(final TestInfo testInfo) {
        userFolder = new UserFolder();
        final Method testMethod = testInfo.getTestMethod().orElseThrow();
        sut = MockValueContainerInjectionTemplate.newTemplate(this, testMethod.getName());
    }

    @Test
    void shouldDefaultValueWhenDuplicatedSettings() {
        // Arrange

        // Act
        sut.accept(Sample.class);

        // Assert
        assertThat(userFolder.getAge(), equalTo(fileAge));
    }

    private static class UserFolder {

        @Value("${file-age-in-sec}")
        private int age;

        public int getAge() {
            return age;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Sample {

    }

}