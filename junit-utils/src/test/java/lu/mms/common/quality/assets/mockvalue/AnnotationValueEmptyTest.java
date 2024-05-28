package lu.mms.common.quality.assets.mockvalue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class AnnotationValueEmptyTest {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    private static final String VALUE_KEY = "annotation_value";

    @InjectMocks
    private DummyTarget sut;

    @MockValue(value = StringUtils.EMPTY)
    private final String atValueProperty = StringUtils.EMPTY;

    @Test
    void shouldSkipMockValueExtensionLogicWhenNoInjectMocksInTestCase() {
        // Arrange
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assert !CollectionUtils.isEmpty(injectMocks) : String.format(MISSING_PARAM_FORMAT, "@InjectMock");

        // Act
        final List<Field> mockValues = AnnotationSupport.findAnnotatedFields(this.getClass(), MockValue.class);

        // Assert
        final Field[] fields = mockValues.toArray(new Field[mockValues.size()]);
        assertThat(fields, arrayWithSize(1));

        final Field field = fields[0];
        assertThat(field.getType(), equalTo(String.class));
        assertThat(field.getName(), equalTo("atValueProperty"));
        assertThat(field.getAnnotations(), arrayWithSize(1));

        Stream.of(field.getAnnotations())
            .map(annotation -> (MockValue) annotation)
            .findAny()
            .ifPresent(mockValue -> assertThat(mockValue.value(), allOf(
                arrayWithSize(1),
                hasItemInArray(StringUtils.EMPTY)
            )));
        assertThat(sut.getTargetValue(), nullValue());
    }

    private static class DummyTarget {

        @Value(VALUE_KEY)
        private String targetValue;

        String getTargetValue() {
            return targetValue;
        }
    }
}
