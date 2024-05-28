package lu.mms.common.quality.assets.mockvalue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Matcher;

import static lu.mms.common.quality.assets.mockvalue.commons.MockValueVisitor.BASIC_VALUE_PATTERN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.text.IsEmptyString.emptyString;

class MockValueDefaultValueTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "${property_name}",
        "${property_name:}",
        "${property_name:the_default_value}",
        "${property_name:42}",
        "${property_name:true}",
        "${property_name:1,2,3}",
        "${property_name:one,two,three}"
    })
    void shouldMatchAtValueBasicSettings(final String value) {
        // Arrange
        final Matcher matcher = BASIC_VALUE_PATTERN.matcher(value);

        // Act
        final boolean isValid = matcher.matches();

        // Assert
        assertThat(isValid, equalTo(true));
        assertThat(matcher.groupCount(), equalTo(2));
        assertThat(matcher.group(1), equalTo("property_name"));
        assertThat(matcher.group(2), notNullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "${property_name}",
        "${property_name:}"
    })
    void shouldMatchAtValueBasicSettingsWhenGroup2IsMissing(final String value) {
        // Arrange
        final Matcher matcher = BASIC_VALUE_PATTERN.matcher(value);

        // Act
        final boolean isValid = matcher.matches();

        // Assert
        assertThat(isValid, equalTo(true));
        assertThat(matcher.groupCount(), equalTo(2));
        assertThat(matcher.group(1), equalTo("property_name"));
        assertThat(matcher.group(2), allOf(
            notNullValue(),
            emptyString()
        ));
    }

    @Test
    void shouldMatchAtValueBasicSettingsWhenGroup2IsDefined() {
        // Arrange
        final String value = "${property_name:42}";
        final Matcher matcher = BASIC_VALUE_PATTERN.matcher(value);

        // Act
        final boolean isValid = matcher.matches();

        // Assert
        assertThat(isValid, equalTo(true));
        assertThat(matcher.groupCount(), equalTo(2));
        assertThat(matcher.group(1), equalTo("property_name"));
        assertThat(matcher.group(2), equalTo("42"));
    }

    @Test
    void shouldMatchAtValueBasicSettingsWhenGroup2IsAnArray() {
        // Arrange
        final String value = "${property_name:one,two,three}";
        final Matcher matcher = BASIC_VALUE_PATTERN.matcher(value);

        // Act
        final boolean isValid = matcher.matches();

        // Assert
        assertThat(isValid, equalTo(true));
        assertThat(matcher.groupCount(), equalTo(2));
        assertThat(matcher.group(1), equalTo("property_name"));
        assertThat(matcher.group(2), equalTo("one,two,three"));
    }
}
