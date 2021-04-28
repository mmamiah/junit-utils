package lu.mms.common.quality.samples.assets.mockvalue;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWith({MockitoExtension.class, MockValueExtension.class})
class MockValueExample7Test {

    @InjectMocks
    private DummyTarget sut;

    // tag::example[]
    @MockValue(
        value = "${int_property:10}",
        testcase = "shouldInitAttributedWhenPropertyIsDefaulted"
    )
    private Integer defaultedIntegerProperty;

    @Test
    void shouldInitAttributedWhenPropertyIsDefaulted() {
        // Arrange / Act
        final Integer value = sut.getIntProperty();

        // Assert
        assertThat(value, notNullValue());
        assertThat(value, equalTo(10));
    }
    // end::example[]

    @Test
    void shouldInitLongAttributedWhenPropertyIsDefaulted() {
        // Arrange / Act
        final Long value = sut.getLongProperty();

        // Assert
        assertThat(value, notNullValue());
    }

    @Test
    void shouldInitStringAttributedWhenPropertyIsDefaulted() {
        // Arrange / Act
        final String value = sut.getStringProperty();

        // Assert
        assertThat(value, notNullValue());
    }

    @Test
    void shouldInitIntArrayAttributedWhenPropertyIsDefaulted() {
        // Arrange / Act
        final int[] actualValues = sut.getIntArrayPropertyWithSingleElement();

        // Assert
        assertThat(actualValues, notNullValue());
    }

    @Test
    void shouldInitLongArrayAttributedWhenPropertyIsDefaulted() {
        // Arrange / Act
        final long[] actualValues = sut.getLongArrayWithMultipleElements();

        // Assert
        assertThat(actualValues, notNullValue());
    }

    // tag::example_context[]
    private static class DummyTarget {

        @Value("${int_property:5}")
        private int intProperty;

        @Value("${long_property:30}")
        private long longProperty;

        @Value("${string_property:hello}")
        private String stringProperty;

        @Value("${int_array_property:236}")
        private int[] intArrayPropertyWithSingleElement;

        @Value("${long_array_property:78,56}")
        private long[] longArrayWithMultipleElements;

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

    }
    // end::example_context[]
}
