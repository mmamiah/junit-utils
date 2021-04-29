package lu.mms.common.quality.samples.assets.mock;

import lu.mms.common.quality.assets.unittest.UnitTest;
import lu.mms.common.quality.samples.models.Customer;
import lu.mms.common.quality.samples.models.Identity;
import lu.mms.common.quality.samples.models.Report;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class UnitTestWithMockAnnotationDefaultTest {

    @InjectMocks
    private Report reportMock;

    @Mock
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldThrowNpeWhenSutInternalMockMethodCalled() {
        // Act / Assert
        assertThrows(NullPointerException.class, () -> reportMock.getCustomerId());
    }

}
