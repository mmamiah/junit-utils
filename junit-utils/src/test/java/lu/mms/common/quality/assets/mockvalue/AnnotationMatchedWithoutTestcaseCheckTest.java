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
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class AnnotationMatchedWithoutTestcaseCheckTest {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    private static final String VALUE_ONE_KEY = "${property_one}";
    private static final String VALUE_TWO_KEY = "${property_two}";

    @InjectMocks
    private DummyTarget sut;

    @MockValue(value = VALUE_ONE_KEY)
    private String atValueProperty = "tc_value_01";

    @MockValue(value = VALUE_TWO_KEY)
    private String atValuePropertyTwo = "tc_value_two";

    @Test
    void shouldFindAllAtValueAnnotationWhenPresentInTestInstanceAndSut() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assert !CollectionUtils.isEmpty(injectMocks) : String.format(MISSING_PARAM_FORMAT, "@InjectMock");

        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);
        assert !CollectionUtils.isEmpty(mockValues) : String.format(MISSING_PARAM_FORMAT, "@MockValue");

        // Act
        final String valueOne = sut.getTargetOne();
        final String valueTwo = sut.getTargetTwo();
        final String valueThree = sut.getTargetThree();

        // Assert
        assertThat(valueOne, equalTo(atValueProperty));
        assertThat(valueTwo, equalTo(atValueProperty));
        assertThat(valueThree, equalTo(atValuePropertyTwo));
    }

    private static class DummyTarget {

        @Value(VALUE_ONE_KEY)
        private String targetOne;

        @Value(VALUE_ONE_KEY)
        private String targetTwo;

        @Value(VALUE_TWO_KEY)
        private String targetThree;

        String getTargetOne() {
            return targetOne;
        }

        String getTargetTwo() {
            return targetTwo;
        }

        String getTargetThree() {
            return targetThree;
        }
    }
}
