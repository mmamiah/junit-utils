package lu.mms.common.quality.assets.mockvalue;

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

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

/**
 * Test to ensure that @{@link Value} is properly set.
 */
@ExtendWith(MockitoExtension.class)
class MockValueExtensionTest {

    private final MockValueExtension mockValueExtension = new MockValueExtension();

    @InjectMocks
    private Customer sut;

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    @MockValue("${customer_name}")
    private String customerName = "Pierre";

    @MockValue(value = "${customer_surname}")
    private final String otherTestcaseSurname = "Billy the Kid";

    @MockValue(
        value = "${customer_surname}",
        testcase = {"shouldInitPropertyWhenTestcaseMatch", "shouldConfirmTheCorrectValueHasBeenSelected"}
    )
    private final String productOwnerSurname = "John WAYNE";

    @MockValue(value = {"${address}", "${country}"})
    private final String location = "Luxembourg";

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
        customerName = "Paul";

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        assertThat(sut.getName(), equalTo(customerName));
        assertThat(sut.getSurname(), equalTo(otherTestcaseSurname));
    }

    @Test
    void shouldInitPropertyWhenTestcaseMatch() {
        // Arrange

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        assertThat(sut.getName(), equalTo(customerName));
        assertThat(sut.getSurname(), equalTo(productOwnerSurname));
    }

    @Test
    void shouldInitMultiplePropertiesWhenConfigured() {
        // Arrange

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        assertThat(sut.getAddress(), equalTo(location));
        assertThat(sut.getCountry(), equalTo(location));
    }

    private static class Customer {
        @Value("${customer_name}")
        private String name;

        @Value("${customer_surname}")
        private String surname;

        @Value("${address}")
        private String address;

        @Value("${country}")
        private String country;

        String getName() {
            return name;
        }

        String getSurname() {
            return surname;
        }

        String getAddress() {
            return address;
        }

        String getCountry() {
            return country;
        }
    }

}
