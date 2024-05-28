package lu.mms.common.quality.userguide.mock;

import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import lu.mms.common.quality.userguide.models.Customer;
import lu.mms.common.quality.userguide.models.Identity;
import lu.mms.common.quality.userguide.models.Report;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWithTestUtils
class ExtendWithTestUtilsWithMockAnnotationDefaultTest {

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
