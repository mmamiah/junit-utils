package lu.mms.common.quality.userguide.mock;

import lu.mms.common.quality.assets.mock.ReturnsMocksExtension;
import lu.mms.common.quality.userguide.models.Customer;
import lu.mms.common.quality.userguide.models.Identity;
import lu.mms.common.quality.userguide.models.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS with #ReturnsMocksExtension.
 */
// tag::example[]
@ExtendWith({MockitoExtension.class, ReturnsMocksExtension.class})
class MockExample4Test {

    private static final Integer ID = 500;

    @InjectMocks
    private Report sut;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldConfirmCustomerIdWhenIdentityIsInitialized() {
        // Arrange
        when(identityMock.getId()).thenReturn(ID);

        // Act
        final Integer id = sut.getCustomerId();

        // Assert
        // confirms that the mock returned is the one we declared in the test class
        assertThat(id, equalTo(ID));
        assertThat(sut.getCustomer().getIdentity(), equalTo(identityMock));
    }
}
// end::example[]
