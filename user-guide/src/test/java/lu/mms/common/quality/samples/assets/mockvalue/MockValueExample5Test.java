package lu.mms.common.quality.samples.assets.mockvalue;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.util.CollectionUtils.isEmpty;

@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class MockValueExample5Test {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    private static final String TC_ALPHA_PROPERTY = "${alpha_property}";
    private static final String TC_ALPHA_VALUE_KEY = "alpha_value";
    private static final String MISSING_TESTCASE = "missing_test_case";
    private static final String TC_BETA_PROPERTY = "${beta_property}";
    private static final String TC_BETA_VALUE_KEY = "beta_value";

    @InjectMocks
    private DummyTarget sut;

    @MockValue(value = TC_ALPHA_PROPERTY, testcase = {"testCaseAlpha", MISSING_TESTCASE})
    private String alpha = TC_ALPHA_VALUE_KEY;

    @MockValue(value = TC_BETA_PROPERTY, testcase = "testCaseBeta")
    private String beta = TC_BETA_VALUE_KEY;

    @Test
    void testCaseBeta() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assumeTrue(!isEmpty(injectMocks), String.format(MISSING_PARAM_FORMAT, InjectMocks.class.getSimpleName()));

        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);
        assumeTrue(!isEmpty(mockValues), String.format(MISSING_PARAM_FORMAT, MockValue.class.getSimpleName()));

        // Act
        final String valueOne = sut.getTargetOne();
        final String valueTwo = sut.getTargetTwo();

        // Assert
        assertThat(valueOne, nullValue());
        assertThat(valueTwo, equalTo(beta));
        assertFields(mockValues.toArray(new Field[mockValues.size()]));
    }

    @Test
    void testCaseAlpha() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assert !isEmpty(injectMocks) : String.format(MISSING_PARAM_FORMAT, "@InjectMock");

        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);
        assert !isEmpty(mockValues) : String.format(MISSING_PARAM_FORMAT, "@MockValue");

        // Act
        final String valueOne = sut.getTargetOne();
        final String valueTwo = sut.getTargetTwo();

        // Assert
        assertThat(valueOne, equalTo(alpha));
        assertThat(valueTwo, nullValue());
        assertFields(mockValues.toArray(new Field[mockValues.size()]));
    }

    @Test
    void shouldNotInjectValueWhenTestCaseNotMatched() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assert !isEmpty(injectMocks) : String.format(MISSING_PARAM_FORMAT, "@InjectMock");

        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);
        assert !isEmpty(mockValues) : String.format(MISSING_PARAM_FORMAT, "@MockValue");

        // Act
        final String valueOne = sut.getTargetOne();
        final String valueTwo = sut.getTargetTwo();

        // Assert
        assertThat(valueOne, nullValue());
        assertThat(valueTwo, nullValue());
        assertFields(mockValues.toArray(new Field[mockValues.size()]));
    }

    private static void assertFields(final Field[] fields) {
        assertThat(fields, Matchers.arrayWithSize(2));

        Stream.of(fields).forEach(field -> {
            assertThat(field.getType(), equalTo(String.class));
            assertThat(field.getAnnotations(), arrayWithSize(1));
        });
    }

    private static class DummyTarget {

        @Value(TC_ALPHA_PROPERTY)
        private String targetOne;

        @Value(TC_BETA_PROPERTY)
        private String targetTwo;

        String getTargetOne() {
            return targetOne;
        }

        String getTargetTwo() {
            return targetTwo;
        }
    }
}
