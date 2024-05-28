package lu.mms.common.quality.assets.mockvalue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.InjectMocks;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.util.CollectionUtils.isEmpty;

@ExtendWith(MockValueExtension.class)
class NoInjectMockSubjectTest {

    private static final String PARAM_ISSUE_FORMAT = "No [@%s] should be defined for this test case.";

    private static final String VALUE_KEY = "${annotation_value:dummy}";

    @MockValue(value = VALUE_KEY)
    private String atValueProperty;

    @Test
    void shouldSkipMockValueExtensionLogicWhenNoInjectMocksInTestCase() {
        // Arrange
        assumeTrue(atValueProperty != null);
        final List<Field> injectMocks = AnnotationSupport.findAnnotatedFields(this.getClass(), InjectMocks.class);
        assumeTrue(isEmpty(injectMocks), String.format(PARAM_ISSUE_FORMAT, InjectMocks.class.getSimpleName()));

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
                hasItemInArray(VALUE_KEY)
            )));
    }
}
