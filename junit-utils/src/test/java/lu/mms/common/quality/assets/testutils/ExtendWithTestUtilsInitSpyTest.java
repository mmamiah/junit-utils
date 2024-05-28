package lu.mms.common.quality.assets.testutils;

import lu.mms.common.quality.assets.mock.MockitoSpyExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit test for {@link MockitoSpyExtension}.
 */
@ExtendWithTestUtils
class ExtendWithTestUtilsInitSpyTest {

    @Spy
    private House houseSpy;

    @Mock
    private Room roomMock;

    @Test
    void shouldConfirmSpyAreInitializedWhenUsingSpyExtension(final InternalMocksContext mocksContext) {
        // Arrange
        assumeTrue(mocksContext.contains(houseSpy));
        assumeTrue(mocksContext.contains(roomMock));

        // Act
        final Room room = houseSpy.getRoom();

        // Assert
        assertThat(room, notNullValue());
        assertThat(room, equalTo(roomMock));
    }

    private static class House {
        private Room room;

        House() {
            // default constructor
        }

        Room getRoom() {
            return room;
        }
    }

    private static class Room {
        // empty class
    }
}
