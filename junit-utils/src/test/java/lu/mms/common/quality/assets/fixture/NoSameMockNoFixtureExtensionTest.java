package lu.mms.common.quality.assets.fixture;

import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoSameMockNoFixtureExtensionTest {

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    @Mock
    private CarService carServiceMock;

    @Fixture
    private FixtureOpenToInjection fixtureOpenToInjection;

    @Fixture
    private FixtureClosedToInjection fixtureClosedToInjection;

    @Fixture
    private NotAnnotatedFixture notAnnotatedFixture;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldNotInitFixtureOpenToInjectionWhenNotTheSameAsInTheFixtureFile() {
        // Arrange
        new FixtureExtension().beforeEach(extensionContextMock);
        assumeTrue(fixtureOpenToInjection != null);

        // Act
        final Exception errorOne = assertThrows(Exception.class, () -> fixtureOpenToInjection.givenBookSizeIsTwenty());

        // Assert
        assertThat(errorOne, instanceOf(NullPointerException.class));
    }

    @Test
    void shouldNotInitFixtureClosedToInjectionWhenNotTheSameAsInTheFixtureFile() {
        // Arrange
        new FixtureExtension().beforeEach(extensionContextMock);
        assumeTrue(fixtureClosedToInjection != null);

        // Act
        final Exception errorOne = assertThrows(Exception.class, () -> fixtureClosedToInjection.givenBookSizeIsTwenty());

        // Assert
        assertThat(errorOne, instanceOf(NullPointerException.class));
    }

    @Test
    void shouldNotInitNoAnnotatedFixtureWhenNotTheSameAsInTheFixtureFile() {
        // Arrange
        new FixtureExtension().beforeEach(extensionContextMock);
        assumeTrue(notAnnotatedFixture != null);

        // Act
        final Exception errorOne = assertThrows(Exception.class, () -> notAnnotatedFixture.givenBookSizeIsTwenty());

        // Assert
        assertThat(errorOne, instanceOf(NullPointerException.class));
    }

}
