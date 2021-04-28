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
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

/**
 * Test to ensure that nothing happen when @{@link InjectMocks} is missing.
 */
@ExtendWith(MockitoExtension.class)
class MockValueExtensionMissingSutTest {

    private final MockValueExtension mockValueExtension = new MockValueExtension();

    private final Customer sut = new Customer();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    @MockValue("${name}")
    private final String customerName = "Pierre Paul";

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldNotSetTheSutPropertyWhenNoTargetToInject() {
        // Arrange

        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        // Injection do not take place as the SUT is missing the @InjectMock annotation.
        assertThat(sut.getName(), nullValue());
    }

    private static class Customer {
        @Value("${name}")
        private String name;

        public String getName() {
            return name;
        }
    }

}
