package lu.mms.common.quality.assets.mockvalue;

import lu.mms.common.quality.assets.mockvalue.commons.MockValueVisitor;
import org.hamcrest.core.AnyOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Matcher;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Test to ensure that @{@link Value} is properly set.
 */
@ExtendWith(MockitoExtension.class)
class MockValueDefaultingTest {

    private final MockValueExtension mockValueExtension = new MockValueExtension();

    @InjectMocks
    private Actor sut;

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    @MockValue("${actor_name}")
    private final String givenName = "EINSTEIN";

    @MockValue(
        value = "${actor_surname}",
        testcase = "shouldInitPropertyWhenTestcaseMatch"
    )
    private final String anotherSurname = "Albert";

    @MockValue(
        value = "${actor_surname:Emil}",
        testcase = {"shouldConfirmTheCorrectValueHasBeenSelected", "shouldInitPropertyWhenTestcaseMatch"}
    )
    private String friendSurname;

    @MockValue(value = "${age}")
    private final int actorAge = 44;

    @MockValue(value = "${country}")
    private final String country = "Luxembourg";

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldInitPropertyWhenConfigurationMatch() {
        // Arrange

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        // equal "Pierre" because it has been defaulted
        assertThat(sut.getName(), equalTo(givenName));
        assertThat(sut.getSurname(), allOf(
                hasItemInArray("James"),
                hasItemInArray("007")
        ));
        assertThat(sut.getManagers(), allOf(hasItemInArray("Emi"), hasItemInArray("Jenny")));
        assertThat(sut.getAge(), equalTo(actorAge));
        assertThat(sut.getCountry(), equalTo(country));
    }

    @Test
    void shouldConfirmTheCorrectValueHasBeenSelected() throws NoSuchFieldException {
        // Arrange
        // Get the expected surname value
        final MockValue mockValueAnnotation = getClass()
            .getDeclaredField("friendSurname")
            .getAnnotation(MockValue.class);
        final Matcher surnameMatcher = MockValueVisitor.BASIC_VALUE_PATTERN.matcher(mockValueAnnotation.value()[0]);
        assumeTrue(surnameMatcher.matches());
        final String expectedSurname = surnameMatcher.group(2);

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        assertThat(sut.getName(), equalTo(givenName));
        assertThat(sut.getSurname(), hasItemInArray(expectedSurname));
        assertThat(sut.getAge(), equalTo(actorAge));
        assertThat(sut.getCountry(), equalTo(country));
    }

    @Test
    void shouldInitPropertyWhenTestcaseMatch() {
        // Arrange

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        assertThat(sut.getName(), equalTo(givenName));
        assertThat(sut.getSurname(), AnyOf.anyOf(hasItemInArray(anotherSurname), hasItemInArray(friendSurname)));
        assertThat(sut.getAge(), equalTo(actorAge));
        assertThat(sut.getCountry(), equalTo(country));
    }

    private static class Actor {
        @Value("${actor_name:BOND}")
        private String name;

        @Value("${actor_surname:James,007}")
        private String[] surname;

        @Value("${age:33}")
        private int age;

        @Value("${country}")
        private String country;

        @Value("${actor_manager:Jenny,Emi}")
        private String[] managers;

        String getName() {
            return name;
        }

        String[] getSurname() {
            return surname;
        }

        int getAge() {
            return age;
        }

        String getCountry() {
            return country;
        }

        public String[] getManagers() {
            return managers;
        }
    }

}
