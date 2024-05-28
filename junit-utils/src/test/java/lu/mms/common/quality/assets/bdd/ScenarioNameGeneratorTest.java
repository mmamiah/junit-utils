package lu.mms.common.quality.assets.bdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;

class ScenarioNameGeneratorTest {

    private final static String DISPLAY_NAME = "should show this display name";

    private final ScenarioNameGenerator sut = new ScenarioNameGenerator();

    @Test
    void shouldMapClassName() {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final String expectedName = "Scenario name generator test";

        // Act
        final String formattedName = sut.generateDisplayNameForClass(targetClass);

        // Assert
        assertThat(formattedName, containsString(expectedName));
    }

    @Test
    void shouldMapNestedClassName() {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final String expectedName = "Scenario name generator test";

        // Act
        final String formattedName = sut.generateDisplayNameForNestedClass(targetClass);

        // Assert
        assertThat(formattedName, containsString(expectedName));
    }

    @Test
    void shouldMapMethodNameWhenDefaultName(final TestInfo testInfo) throws NoSuchMethodException {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final Method method = testInfo.getTestMethod().get();

        // Act
        final String formattedName = sut.generateDisplayNameForMethod(targetClass, method);

        // Assert
        assertThat(formattedName, equalTo("Should map method name when default name"));
    }

    @Test
    void shouldMapNameContainingAConjunction(final TestInfo testInfo) throws NoSuchMethodException {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final Method method = testInfo.getTestMethod().get();

        // Act
        final String formattedName = sut.generateDisplayNameForMethod(targetClass, method);

        // Assert
        assertThat(formattedName, equalTo("Should map name containing a conjunction"));
    }

    @Test
    void shouldMapTestNameLikeCTest(final TestInfo testInfo) throws NoSuchMethodException {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final Method method = testInfo.getTestMethod().get();

        // Act
        final String formattedName = sut.generateDisplayNameForMethod(targetClass, method);

        // Assert
        assertThat(formattedName, equalTo("Should map test name like C. test"));
    }

    @Test
    void should_map_method_name_when_snake_case_name(final TestInfo testInfo) throws NoSuchMethodException {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final Method method = testInfo.getTestMethod().get();

        // Act
        final String formattedName = sut.generateDisplayNameForMethod(targetClass, method);

        // Assert
        assertThat(formattedName, equalTo("Should map method name when snake case name"));
    }

    @Test
    @DisplayName(DISPLAY_NAME)
    void shouldMapMethodNameWhenDisplayName(final TestInfo testInfo) throws NoSuchMethodException {
        // Arrange
        final Class<?> targetClass = ScenarioNameGeneratorTest.class;
        final Method method = testInfo.getTestMethod().get();

        // Act
        final String formattedName = sut.generateDisplayNameForMethod(targetClass, method);

        // Assert
        assertThat(formattedName, equalTo(DISPLAY_NAME));
    }

}
