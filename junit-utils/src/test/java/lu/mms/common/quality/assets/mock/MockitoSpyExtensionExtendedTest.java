package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.oneOf;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Unit test for {@link MockitoSpyExtension}.
 */
@ExtendWith({MockitoExtension.class, MockitoSpyExtension.class, MockValueExtension.class})
class MockitoSpyExtensionExtendedTest {

    private final MockitoSpyExtension sut = new MockitoSpyExtension();

    @MockValue("${high}")
    private final Integer high = 10;

    @Spy
    private House houseSpy;

    @Mock
    private Room defaultRoomMock;

    @Mock(name = "room")
    private Room roomMock;

    @Test
    void shouldConfirmSizePropertyIsInitialized() {
        // Arrange

        // Act
        final Integer size = houseSpy.getSize();

        // Assert
        assertThat(size, equalTo(5));
    }

    @Test
    void shouldConfirmHighPropertyIsInitialized() {
        // Arrange

        // Act
        final Integer high = houseSpy.getHigh();

        // Assert
        assertThat(high, equalTo(10));
    }

    @Test
    void shouldConfirmSpyAreInitializedWhenUsingSpyExtension() {
        // Arrange

        // Act

        // Assert
        final Room room = houseSpy.getRoom();
        assertThat(room, notNullValue());
        assertThat(room, equalTo(roomMock));

        final Room vipRoom = houseSpy.getVipRoom();
        assertThat(vipRoom, notNullValue());
        assertThat(room, oneOf(roomMock, defaultRoomMock));

        final List<Room> rooms = houseSpy.getRooms();
        assertThat(rooms, iterableWithSize(2));
    }

    private static class House {
        @Value("${size:5}")
        private Integer size;

        @Value("${high}")
        private Integer high;

        private Room room;
        private Room vipRoom;
        private List<Room> rooms;

        House() {
            // default constructor
        }

        public Integer getSize() {
            return size;
        }

        public Integer getHigh() {
            return high;
        }

        public List<Room> getRooms() {
            return rooms;
        }

        Room getRoom() {
            return room;
        }

        Room getVipRoom() {
            return vipRoom;
        }
    }

    private static class Room {
        // empty class
    }
}
