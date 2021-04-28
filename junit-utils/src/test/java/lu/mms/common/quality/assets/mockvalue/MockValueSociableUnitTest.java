package lu.mms.common.quality.assets.mockvalue;

import lu.mms.common.quality.assets.mockvalue.commons.MockValueVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Matcher;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.MODEL_MAPPER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class MockValueSociableUnitTest {

    private static final String MISSING_PARAM_FORMAT = "[@%s] should be defined for this test case.";

    @InjectMocks
    private DummyTarget sut;

    @MockValue(value = "${int_property:10}", testcase = "shouldInitAttributedWhenPropertyIsDefaulted")
    private Integer defaultedIntegerProperty;

    @MockValue(value = "${int_property:10}", testcase = "shouldInitAttributedWhenPropertyInitialized")
    private Integer initializedIntegerProperty = 20005;

    @Test
    void shouldNotInitAttributedWhenInvalidProperty() {
        // Arrange

        // Act
        final Object value = sut.getInvalidProperty();

        // Assert
        assertThat(value, nullValue());
    }

    @Test
    void shouldNotInitAttributedWhenPropertyHasNoCandidateValue() {
        // Arrange

        // Act
        final Integer value = sut.getIntPropertyNotDefaulted();

        // Assert
        assertThat(value, nullValue());
    }

    @Test
    void shouldInitAttributedWhenPropertyIsDefaulted() {
        // Arrange / Act
        final Integer value = sut.getIntProperty();

        // Assert
        assertThat(value, notNullValue());
    }

    @Test
    void shouldInitAttributedWhenPropertyInitialized() throws NoSuchFieldException {
        // Arrange
        // Get the expected surname value
        final MockValue mockValueAnnotation = getClass()
            .getDeclaredField("defaultedIntegerProperty")
            .getAnnotation(MockValue.class);
        final Matcher surnameMatcher = MockValueVisitor.BASIC_VALUE_PATTERN.matcher(mockValueAnnotation.value()[0]);
        assumeTrue(surnameMatcher.matches());
        final Integer defaultProperty = Integer.valueOf(surnameMatcher.group(2));
        assumeFalse(initializedIntegerProperty.equals(defaultProperty));

        // Act
        final Integer value = sut.getIntProperty();

        // Assert
        assertThat(value, notNullValue());
        assertThat(value, equalTo(initializedIntegerProperty));
    }

    @Test
    void shouldInitLongAttributedWhenPropertyIsDefaulted() throws NoSuchFieldException {
        // Arrange
        // Get the expected surname value
        final Value valueAnnotation = sut.getClass()
            .getDeclaredField("longProperty")
            .getAnnotation(Value.class);
        final Matcher surnameMatcher = MockValueVisitor.BASIC_VALUE_PATTERN.matcher(valueAnnotation.value());
        assumeTrue(surnameMatcher.matches());
        final Long defaultProperty = Long.valueOf(surnameMatcher.group(2));

        // Act
        final Long value = sut.getLongProperty();

        // Assert
        assertThat(value, notNullValue());
        assertThat(value, equalTo(defaultProperty));
    }

    @Test
    void shouldInitStringAttributedWhenPropertyIsDefaulted() throws NoSuchFieldException {
        // Arrange
        // Get the expected surname value
        final Value valueAnnotation = sut.getClass()
            .getDeclaredField("stringProperty")
            .getAnnotation(Value.class);
        final Matcher surnameMatcher = MockValueVisitor.BASIC_VALUE_PATTERN.matcher(valueAnnotation.value());
        assumeTrue(surnameMatcher.matches());
        final String defaultProperty = surnameMatcher.group(2);

        // Act
        final String value = sut.getStringProperty();

        // Assert
        assertThat(value, notNullValue());
        assertThat(value, equalTo(defaultProperty));
    }

    @Test
    void shouldInitIntArrayAttributedWhenPropertyIsDefaulted() throws NoSuchFieldException {
        // Arrange
        // Get the expected surname value
        final Value valueAnnotation = sut.getClass()
            .getDeclaredField("intArrayPropertyWithSingleElement")
            .getAnnotation(Value.class);
        final Matcher surnameMatcher = MockValueVisitor.BASIC_VALUE_PATTERN.matcher(valueAnnotation.value());
        assumeTrue(surnameMatcher.matches());
        int[] actualValues = new int[0];
        final int[] defaultProperties = MODEL_MAPPER.map(surnameMatcher.group(2).split(","), actualValues.getClass());

        // Act
        actualValues = sut.getIntArrayPropertyWithSingleElement();

        // Assert
        assertThat(actualValues, notNullValue());
        assertThat(actualValues, equalTo(defaultProperties));
    }

    @Test
    void shouldInitLongArrayAttributedWhenPropertyIsDefaulted() throws NoSuchFieldException {
        // Arrange
        // Get the expected surname value
        final Value valueAnnotation = sut.getClass()
            .getDeclaredField("longArrayWithMultipleElements")
            .getAnnotation(Value.class);
        final Matcher surnameMatcher = MockValueVisitor.BASIC_VALUE_PATTERN.matcher(valueAnnotation.value());
        assumeTrue(surnameMatcher.matches());
        long[] actualValues = new long[0];
        final long[] defaultProperties = MODEL_MAPPER.map(surnameMatcher.group(2).split(","), actualValues.getClass());

        // Act
        actualValues = sut.getLongArrayWithMultipleElements();

        // Assert
        assertThat(actualValues, notNullValue());
        assertThat(actualValues, equalTo(defaultProperties));
    }

    private static class DummyTarget {

        @Value("invalid_property")
        private Object invalidProperty;

        @Value("${int_property:5}")
        private int intProperty;

        @Value("${long_property:30}")
        private long longProperty;

        @Value("${string_property:hello}")
        private String stringProperty;

        @Value("${int_array_property:236}")
        private int[] intArrayPropertyWithSingleElement;

        @Value("${long.array-property:78,56}")
        private long[] longArrayWithMultipleElements;

        @Value("${int_not_defaulted_property}")
        private Integer intPropertyNotDefaulted;

        Object getInvalidProperty() {
            return invalidProperty;
        }

        int getIntProperty() {
            return intProperty;
        }

        long getLongProperty() {
            return longProperty;
        }

        String getStringProperty() {
            return stringProperty;
        }

        int[] getIntArrayPropertyWithSingleElement() {
            return intArrayPropertyWithSingleElement;
        }

        long[] getLongArrayWithMultipleElements() {
            return longArrayWithMultipleElements;
        }

        Integer getIntPropertyNotDefaulted() {
            return intPropertyNotDefaulted;
        }
    }
}
