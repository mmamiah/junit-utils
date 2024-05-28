package lu.mms.common.quality.assets.mockvalue;

import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
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
 * Test to ensure that nothing happen when @{@link MockValue} mismatch.
 */
@ExtendWith(MockitoExtension.class)
class MockValueExtensionMissingAnnotationValueMismatchTest {

    private final MockValueExtension mockValueExtension = new MockValueExtension();

    @InjectMocks
    private ProductOwner sut;

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    @MockValue("${po_name_mismatch}")
    private final String productOwnerName = "Pierre Paul";

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldNotSetTheSutPropertyWhenAnnotationValueMismatch() {
        // Act
        mockValueExtension.beforeEach(extensionContextMock);

        // Assert
        assertThat(sut.getName(), nullValue());
    }

    private static class ProductOwner {
        @Value("${po_name}")
        private String name;

        public String getName() {
            return name;
        }
    }

}
