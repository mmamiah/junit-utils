package lu.mms.common.quality.assets.mockvalue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;

@ExtendWith(MockValueExtension.class)
class NoMockValueAnnotationTest {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    private static final String VALUE_KEY = "temp_value";

    @InjectMocks
    private DummyTarget sut;

    @Test
    void shouldFindAllAtValueAnnotationWhenPresentInTestInstance() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assert !CollectionUtils.isEmpty(injectMocks) : String.format(MISSING_PARAM_FORMAT, "@InjectMock");

        // Act
        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);

        // Assert
        assertThat(mockValues.toArray(), arrayWithSize(0));
    }

    private static class DummyTarget {

        @Value(VALUE_KEY)
        private String targetValue;

    }
}
