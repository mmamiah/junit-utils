package lu.mms.common.quality.userguide.mock;

import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import lu.mms.common.quality.userguide.models.Customer;
import lu.mms.common.quality.userguide.models.Identity;
import lu.mms.common.quality.userguide.models.Report;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS and #ExtendWithTestUtils.answer is false.
 */
@ExtendWithTestUtils
class ExtendWithTestUtilsWithMockAnnotationReturnsMockTest {

    private static final Integer ID = 500;

    @InjectMocks
    private Report sut;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldReturnTestcaseMockWhenSutInternalMockCalled() {
        // Arrange
        when(identityMock.getId()).thenReturn(ID);

        final Answer<?> defaultAnswer = MockUtil.getMockSettings(customerMock).getDefaultAnswer();
        Assumptions.assumeTrue(defaultAnswer.toString().equals(Answers.RETURNS_MOCKS.name()));

        // Act
        final Integer id = sut.getCustomerId();

        // Assert
        assertThat(id, equalTo(ID));

        // assert that the mock returned by one of our declared mock, is the also one of our declared mock.
        assertThat(sut.getCustomer().getIdentity(), equalTo(identityMock));

    }

}
