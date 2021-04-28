package lu.mms.common.quality.samples.assets.mockvalue;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.unittest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@UnitTest
class MockValueExample6Test {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    private static final String VALUE_ONE_KEY = "${property_one}";
    private static final String VALUE_TWO_KEY = "${property_two}";

    @InjectMocks
    private DummyTarget sut;

    @MockValue(value = VALUE_ONE_KEY)
    private String atValueProperty = "value_01";

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

        // Assert
        assertThat(valueOne, equalTo(atValueProperty));
        assertThat(valueTwo, equalTo(atValueProperty));
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
