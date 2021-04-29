package lu.mms.common.quality.samples.assets.mock;

import lu.mms.common.quality.assets.unittest.UnitTest;
import lu.mms.common.quality.samples.models.Customer;
import lu.mms.common.quality.samples.models.Identity;
import lu.mms.common.quality.samples.models.Report;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS and #UnitTest.answer is false.
 */
@UnitTest(returnMocks = false)
class UnitTestWithDefaultMockAnnotationAnswerTest {

    private static final Integer ID = 987456;

    @InjectMocks
    private Report sut;

    @Mock
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldNotReturnMockWhenSutInternalMockMethodCalled() {
        // Arrange
        assertThat(new Identity().getId(), equalTo(ID));

        // Act
        final Customer customer = sut.getCustomer();

        // Assert
        assertThat(customer, equalTo(customerMock));
        assertThat(customer.getIdentity(), nullValue());
    }

}
