package lu.mms.common.quality.samples.assets.mock;

import lu.mms.common.quality.assets.mock.ReturnsMocksExtension;
import lu.mms.common.quality.samples.models.Customer;
import lu.mms.common.quality.samples.models.Report;
import lu.mms.common.quality.samples.models.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS with #ReturnsMocksExtension.
 */
// tag::example[]
@ExtendWith(ReturnsMocksExtension.class)
class MockExample3Test {

    private static final Integer ID = 500;

    private Report sut;
    private Customer customerMock;
    private Identity identityMock;

    @BeforeEach
    void init() {
        // Prepare mocks
        identityMock = Mockito.mock(Identity.class);
        customerMock = Mockito.mock(Customer.class, Answers.RETURNS_MOCKS);

        // Prepare SUT
        sut = new Report();
        ReflectionTestUtils.setField(sut, "customer", customerMock);
    }

    @Test
    void shouldConfirmCustomerIdWhenIdentityMockIsShared() {
        // Arrange
        when(identityMock.getId()).thenReturn(ID);

        // Act
        final Integer id = sut.getCustomerId();

        // Assert
        assertThat(id, equalTo(ID));

        // assert that the mock returned is the one we declared.
        assertThat(sut.getCustomer().getIdentity(), equalTo(identityMock));
    }
}
// end::example[]
