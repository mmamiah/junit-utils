package lu.mms.common.quality.userguide.mock;

import lu.mms.common.quality.userguide.models.Customer;
import lu.mms.common.quality.userguide.models.Identity;
import lu.mms.common.quality.userguide.models.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

// tag::example[]
class MockExample1Test {

    private static final Integer ID = 500;

    private Report sut;
    private Customer customerMock;
    private Identity identityMock;

    @BeforeEach
    void init() {
        // Prepare mocks
        identityMock = Mockito.mock(Identity.class);
        customerMock = Mockito.mock(Customer.class);
        when(customerMock.getIdentity()).thenReturn(identityMock);

        // Prepare SUT
        sut = new Report();
        ReflectionTestUtils.setField(sut, "customer", customerMock);
    }

    @Test
    void shouldConfirmCustomerIdWhenIdentityHasValidId() {
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
