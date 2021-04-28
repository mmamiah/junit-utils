package lu.mms.common.quality.samples.assets.mock;

import lu.mms.common.quality.assets.unittest.UnitTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS and #UnitTest.answer is false.
 */
@UnitTest(returnMocks = false)
class UnitTestWithMockAnnotationReturnMockTest {

    private static final String ID = "dummy-id";

    @InjectMocks
    private CustomerManager sut;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldReturnNewMockWhenSutInternalMockMethodCalled() {
        // Arrange
        final Answer<?> defaultAnswer = MockUtil.getMockSettings(customerMock).getDefaultAnswer();
        Assumptions.assumeTrue(defaultAnswer.toString().equals(Answers.RETURNS_MOCKS.name()));

        // Act
        final String id = sut.getCustomerId();

        // Assert
        verify(identityMock, never()).getId();
        assertThat(id, not(equalTo(ID)));

        // assert that the mock returned by one of our declared mock, is the also one of our declared mock.
        assertThat(sut.getCustomer().getIdentity(), not(equalTo(identityMock)));

    }

    private static class CustomerManager {
        private Customer customer;

        String getCustomerId() {
            return customer.getIdentity().getId();
        }

        Customer getCustomer() {
            return customer;
        }
    }

    private static class Customer {
        private Identity identity;

        Identity getIdentity() {
            return identity;
        }
    }

    private static class Identity {
        private String id;

        String getId() {
            return id;
        }
    }

}
