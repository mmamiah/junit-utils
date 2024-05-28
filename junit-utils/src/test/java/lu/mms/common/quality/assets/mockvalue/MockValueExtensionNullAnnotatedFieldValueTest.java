package lu.mms.common.quality.assets.mockvalue;

import lu.mms.common.quality.JunitUtilsPreconditionException;
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
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Test to ensure that nothing happen when @{@link MockValue} annotated field have a NULL value.
 */
@ExtendWith(MockitoExtension.class)
class MockValueExtensionNullAnnotatedFieldValueTest {

    private final MockValueExtension mockValueExtension = new MockValueExtension();

    @InjectMocks
    private ProductOwner sut;

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    @MockValue("${po_name}")
    private final String nullProductOwnerName = null;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldThrowExceptionWhenWhenAnnotatedFieldIsNull(final TestInfo testInfo) {
        // Arrange

        // Act
        Exception exception = assertThrows(
            Exception.class,
            () -> mockValueExtension.beforeEach(extensionContextMock)
        );

        // Assert
        assertThat(exception, instanceOf(JunitUtilsPreconditionException.class));
    }

    private static class ProductOwner {
        @Value("${po_name}")
        private String name;

        public String getName() {
            return name;
        }
    }

}
