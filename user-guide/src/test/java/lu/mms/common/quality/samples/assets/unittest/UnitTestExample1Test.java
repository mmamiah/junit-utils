package lu.mms.common.quality.samples.assets.unittest;

import lu.mms.common.quality.assets.unittest.UnitTest;
import lu.mms.common.quality.samples.models.Customer;
import lu.mms.common.quality.samples.models.Report;
import lu.mms.common.quality.samples.models.Identity;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

// tag::example[]
@UnitTest
class UnitTestExample1Test {

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
