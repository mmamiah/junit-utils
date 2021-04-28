package lu.mms.common.quality.assets.mockvalue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class AnnotationNotMatchedTest {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    private static final String VALUE_KEY = "annotation_value";

    @InjectMocks
    private DummyTarget sut;

    @MockValue(value = VALUE_KEY)
    private String atValueProperty = "test_case_value";

    @Test
    void shouldNotFindAllAtValueAnnotationWhenPresentInTestInstanceAndMissingInSutObject() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assert !CollectionUtils.isEmpty(injectMocks) : String.format(MISSING_PARAM_FORMAT, "@InjectMock");

        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);
        assert !CollectionUtils.isEmpty(mockValues) : String.format(MISSING_PARAM_FORMAT, "@MockValue");

        final List<Field> extendsWith = AnnotationSupport.findAnnotatedFields(this.getClass(), ExtendWith.class);

        // Act
        final String value = sut.getTargetValue();

        // Assert
        assertThat(value, nullValue());
    }

    private static class DummyTarget {

        @Value("another_value")
        private String targetValue;

        String getTargetValue() {
            return targetValue;
        }
    }
}
