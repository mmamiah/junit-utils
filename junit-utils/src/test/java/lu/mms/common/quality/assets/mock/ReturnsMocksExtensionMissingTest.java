package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testing the {@link ReturnsMocksExtension}.
 */
@ExtendWith(MockitoExtension.class)
class ReturnsMocksExtensionMissingTest {

    @Mock
    private Room roomMock;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private House houseMock;


    @Test
    void shouldNotInitMocksWhenMissingExtendWithTestUtilsAnnotation() {
        // Arrange
        assumeTrue(getClass().getDeclaredAnnotation(ExtendWithTestUtils.class) == null);

        // Act
        final Room room = houseMock.getRoom();

        // Assert
        assertThat(room, allOf(notNullValue(), not(roomMock)));

        // ensure that the unique mock in our class didn't change
        final Answer<?> contextAnswer = MockUtil.getMockSettings(houseMock).getDefaultAnswer();
        assertThat(contextAnswer, equalTo(Answers.RETURNS_MOCKS));
        assertThat(contextAnswer, not(instanceOf(ReturnsMocksAnswer.class)));
    }

    static class Room {
        // empty class
    }

    static class House {
        private Room room;

        public Room getRoom() {
            return room;
        }
    }
}
