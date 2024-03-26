package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.MockInjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWith({MockitoExtension.class, MockInjectionExtension.class})
@Disabled
class TypeMismatchOnMockInjectionExtensionTest {

    private static final String BY_CONSTRUCTOR_TEMPLATE = "by_constructor_%s";
    private static final String BY_METHOD_TEMPLATE = "by_method_%s";

    @InjectMocks
    private Laptop sut;

    // This case is not yet properly handled in the context
    // TODO (mmamiah) Under which type should we register such collection/array in the context.
    // My first though is to register it under <Device>, but how to handle it's retrieval then ?
    @Spy
    private final List<Device> devices = new ArrayList<>();

    @BeforeEach
    private void init() {
        devices.add(new SuperMice());
    }

    /**
     * This testcase, ensure that we keep instantiating the SUT with user mocks, when a Test instance has a declared
     * field which name is the same with a SUT field, but their type is not the same (type mismatch). <br>
     * Example: See the "customers" field.
     * <code>
     * class TestClass {
     *      @InjectMock
     *      private Restaurant sut;
     *
     *      List<String> customers;
     * }
     *
     * class Restaurant {
     *      private Map<String, Integer> customers;
     * }
     * </code>
     */
    @Test
    void shouldRebuildTheSutWithUserMockWhenMockNameMatchAndTypeMismatch() {
        // Arrange

        // Act
        final Map<String, Device> sutDevices = sut.getDevices();
        final Map<String, Device> sutConstructed = sut.getDevicesByConstructor();

        // Assert
        assertThat(sutDevices, allOf(
            notNullValue(),
            anEmptyMap()
        ));
        assertThat(MockUtil.isMock(sutDevices), equalTo(false));
        assertThat(sutConstructed, allOf(
            notNullValue(),
            anEmptyMap()
        ));
        assertThat(MockUtil.isMock(sutConstructed), equalTo(false));
    }

    static class Laptop {

        // --- attributes initialized via constructor
        final private Map<String, Device> devicesByConstructor;

        private Map<String, Device> devices;

        Laptop (final List<Device> devices) {
            this.devicesByConstructor = Stream.ofNullable(devices)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(
                            item -> String.format(BY_CONSTRUCTOR_TEMPLATE, MockUtil.getMockName(item).toString()),
                            Function.identity())
                    );
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

        public Map<String, Device> getDevices() {
            return devices;
        }
    }

    // Interface used to group the mocks
    interface Device {
        // empty interface
    }

    // extending another a class implementing the desired interface
    static class Mice implements Device {
        // empty class
    }

    static class SuperMice implements Device {
        // empty class
    }


}