package lu.mms.common.quality.userguide.testutils;

import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import lu.mms.common.quality.userguide.models.Customer;
import lu.mms.common.quality.userguide.models.Identity;
import lu.mms.common.quality.userguide.models.Report;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

// tag::example[]
@ExtendWithTestUtils
class ExtendWithTestUtilsExample1Test {

    @InjectMocks
    private Report sut;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Customer customerMock;

    @Mock
    private Identity identityMock;

    @Test
    void shouldEnsureThatIdentityIsRecognizedInTestcase() {
        // Arrange
        final Integer id = 456;
        when(identityMock.getId()).thenReturn(id);
        // Act
        final Integer result = sut.getCustomerId();

        // Asset
        assertThat(result, equalTo(id));
    }
}
// end::example[]
