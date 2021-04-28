package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.unittest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS and #UnitTest.answer is false.
 */
@ExtendWith({MockitoExtension.class, ReturnsMocksExtension.class})
class ReturnsMocksExtensionTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Building sut;

    @Mock
    private Owner ownerMock;

    @Test
    void shouldReturnTestcaseMockWhenMockWithReturnMockAnswersCalled() {
        // Arrange
        assumeTrue(getClass().getDeclaredAnnotation(UnitTest.class) == null);
        Answer<?> buildingMockAnswer = MockUtil.getMockSettings(sut).getDefaultAnswer();
        assumeTrue(buildingMockAnswer.toString().equals(Answers.RETURNS_MOCKS.name()));

        Answer<?> ownerMockAnswer = MockUtil.getMockSettings(ownerMock).getDefaultAnswer();
        assumeTrue(ownerMockAnswer.toString().equals(Answers.RETURNS_DEFAULTS.name()));

        // Act
        final Owner result = sut.getOwner();

        // Assert
        assertThat(result, equalTo(ownerMock));

        buildingMockAnswer = MockUtil.getMockSettings(sut).getDefaultAnswer();
        assertThat(buildingMockAnswer.toString().equals(Answers.RETURNS_MOCKS.name()), equalTo(true));

        // assert that the Answer implementation have been properly configured
        final Object answerImplementation = ReflectionTestUtils.getField(buildingMockAnswer, "implementation");
        assertThat(answerImplementation, instanceOf(ReturnsMocksAnswer.class));

        ownerMockAnswer = MockUtil.getMockSettings(ownerMock).getDefaultAnswer();
        assertThat(ownerMockAnswer.toString(), equalTo(Answers.RETURNS_DEFAULTS.name()));
    }

    // Test case inline model

    private static class Building {
        private Owner owner;

        Owner getOwner() {
            return owner;
        }
    }

    private static class Owner {
        private String name;

        String getName() {
            return name;
        }
    }

}
