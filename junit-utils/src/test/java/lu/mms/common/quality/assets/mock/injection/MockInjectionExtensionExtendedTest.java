package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.MockInjectionExtension;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.collection.IsIn.oneOf;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Ensure that the collection of mocks (List, Set, Collection) as well as the arrays, are properly instanciated and
 * initialized with the mocks defined by the user.
 */
@ExtendWith({MockitoExtension.class, MockInjectionExtension.class})
class MockInjectionExtensionExtendedTest {

    private static final String BY_CONSTRUCTOR_TEMPLATE = "by_constructor_%s";
    private static final String BY_METHOD_TEMPLATE = "by_method_%s";

    @InjectMocks
    private Computer sut;

    @Mock
    private PointingDevice pointingDeviceMock;

    @Mock
    private Mice miceMock;

    @Spy
    private Keyboard keyboardSpy;

    @Test
    void shouldInjectMockViaAutowiredConstructor() {
        // Arrange

        // Act
        final Map<String, Device> items = sut.getDevicesByConstructor();

        // Assert
        assertThat(items, IsNot.not(anEmptyMap()));
        assertThat(items.values(), hasItem(oneOf(pointingDeviceMock, miceMock, keyboardSpy)));
    }

    @Test
    void shouldInjectMockWhenAutowiredMethodCalled() {
        // Arrange

        // Act

        // Assert
        assertThat(sut.getValueTwo(), notNullValue());
        assertThat(sut.getDevices(), notNullValue());
    }

    @Test
    void shouldInitTheMocksCollection() {
        // Arrange

        // Act

        // Assert
        assertThat(sut.getCollection(), iterableWithSize(3));
        assertThat(sut.getList(), iterableWithSize(3));
        assertThat(sut.getSet(), iterableWithSize(3));
        assertThat(sut.getArray(), arrayWithSize(2));
    }

    static class Computer {
        // --- attributes initialized via constructor
        private final Map<String, Device> devicesByConstructor;

        // --- attributes initialized via @Autowired methods
        private Object valueTwo;
        private Map<String, Device> devices;

        private Collection<Device> collection;

        private List<Device> list;

        private Set<Device> set;

        private PointingDevice[] array;

        public Computer(final List<Device> devices) {
            this.devicesByConstructor = Stream.ofNullable(devices)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(
                            item -> String.format(BY_CONSTRUCTOR_TEMPLATE, MockUtil.getMockName(item).toString()),
                            Function.identity())
                    );
        }

        @Autowired
        private void initOne(final String aValue) {
            this.valueTwo = StringUtils.defaultString(aValue);
        }

        @Autowired
        private void initTwo(final List<Device> aValue) {
            this.devices = aValue.stream().collect(Collectors.toMap(
                    item -> String.format(BY_METHOD_TEMPLATE, MockUtil.getMockName(item).toString()),
                    Function.identity()));
        }

        public Map<String, Device> getDevicesByConstructor() {
            return devicesByConstructor;
        }

        public Object getValueTwo() {
            return valueTwo;
        }

        public Map<String, Device> getDevices() {
            return devices;
        }

        public Collection<Device> getCollection() {
            return collection;
        }

        public List<Device> getList() {
            return list;
        }

        public Set<Device> getSet() {
            return set;
        }

        public PointingDevice[] getArray() {
            return array;
        }
    }

    // Interface used to group the mocks
    interface Device {
        // empty interface
    }

    // simple example of use of the interface
    static class PointingDevice implements Device {
        // empty class
    }

    // extending another a class implementing the desired interface
    static class Mice extends PointingDevice {
        // empty class
    }

    // Another interface implementation to check multiple interface implementation
    static class Keyboard implements Device {
        // empty class
    }

}
