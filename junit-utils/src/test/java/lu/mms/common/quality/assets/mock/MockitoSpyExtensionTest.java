package lu.mms.common.quality.assets.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.oneOf;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link MockitoSpyExtension}.
 */
@ExtendWith(MockitoExtension.class)
class MockitoSpyExtensionTest {

    private final MockitoSpyExtension sut = new MockitoSpyExtension();

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);


    @Spy
    private House houseSpy;

    @Mock
    private Room defaultRoomMock;

    @Mock(name = "room")
    private Room roomMock;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldConfirmSpyIsNotInitializedWhenNotUsingSpyExtension() {
        // Arrange  / Act
        // --> not initialized the extension

        // Assert
        final Room room = houseSpy.getRoom();
        assertThat(room, nullValue());

        final Room vipRoom = houseSpy.getVipRoom();
        assertThat(vipRoom, nullValue());
    }

    @Test
    void shouldConfirmSpyAreInitializedWhenUsingSpyExtension() {
        // Arrange

        // Act
        sut.beforeEach(extensionContextMock);

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
        private Room room;
        private Room vipRoom;
        private List<Room> rooms;

        House() {
            // default constructor
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
