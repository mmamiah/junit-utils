package lu.mms.common.quality.samples.assets.mock;

import lu.mms.common.quality.samples.models.Customer;
import lu.mms.common.quality.samples.models.Report;
import lu.mms.common.quality.samples.models.Identity;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS without #ReturnsMocksExtension.
 */
// tag::example[]
class MockExample2Test {

    private static final Integer ID = 500;

    private Report sut;
    private Customer customerMock;
    private Identity identityMock;

    @BeforeEach
    void init() {
        // Prepare mocks
        identityMock = Mockito.mock(Identity.class);
        customerMock = Mockito.mock(Customer.class, Answers.RETURNS_MOCKS);
        // when(customerMock.getIdentity()).thenReturn(identityMock);

        // Prepare SUT
        sut = new Report();
        ReflectionTestUtils.setField(sut, "customer", customerMock);
    }

    @Test
    void shouldNotFindCustomerIdWhenIdentityMockDiffers() {
        // Arrange
        when(identityMock.getId()).thenReturn(ID);

        // Act
        final Integer id = sut.getCustomerId();

        // Assert

        // We will keep in following example to assert that the mock returned
        // is the one we declared, but by now, to keep the test passing we will
        // assert that the mock is different.
        assertThat(id, notNullValue());
        verify(identityMock, never()).getId();

        // Just to confirm we are not talking about the same mock.
        assertThat(id, IsNot.not(ID));
        assertThat(sut.getCustomer().getIdentity(), IsNot.not(identityMock));
    }
}
// end::example[]
