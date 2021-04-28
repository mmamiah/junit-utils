package lu.mms.common.quality.assets.mock.reinforcement;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.context.MocksContextParameterResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith({MockitoExtension.class, MocksContextParameterResolver.class, ReinforceMockExtension.class})
class ReinforceMockExtensionTest {

    @InjectMocks
    private Parking sut;

    @Spy
    private Car carSpy;

    // Having a field with the same type for system proof. Mockito natural skip such field. We will prefer the @Spy
    // over this field, or a mock which mock name matches.
    @Mock
    private Car carMock;

    @Mock
    private Bus busMock;

    @Mock
    private Engine engineMock;

    @BeforeEach
    void applyTestCaseAssumptions(final InternalMocksContext mocksContext) {
        assumeFalse(mocksContext.contains(sut));
        assumeTrue(mocksContext.contains(busMock));
        assumeTrue(mocksContext.contains(engineMock));
    }

    @Test
    void shouldEnsureThatSutIsProperlyInitialized() {
        // Arrange

        // Act
        final Car car = sut.getCar();
        final Engine engine = sut.getEngine();

        // Assert
        // Field injection ignored by Mockito because we have declared two mocks of the same type (carMock & carSpy)
        assertThat(car, nullValue());

        assertThat(engine, equalTo(engineMock));
    }

    @Test
    void shouldEnsureThatSpyAreIgnored() {
        // Arrange

        // Act
        final Engine engine = carSpy.getEngine();

        // Assert
        assertThat(engine, nullValue());
    }

    @Test
    void shouldEnsureThatMockAreProperlyInitialized(final InternalMocksContext mocksContext) {
        // Arrange

        // Act
        final Engine engine = (Engine) ReflectionTestUtils.getField(busMock,"engine");

        // Assert
        assertThat(engine, notNullValue());
        assertThat(engine, equalTo(engineMock));

        assertNotNullMembers(busMock);
    }

    private void assertNotNullMembers( final Object target) {
        Class<?> targetClass = MockUtil.getMockSettings(target).getTypeToMock();
        Arrays.stream(targetClass.getDeclaredFields())
                .forEach(field -> {
                    final Object member = ReflectionTestUtils.getField(target, field.getName());
                    assertThat(member, notNullValue());
                });
    }

    private static class Parking {
        private Car car;
        private Bus bus;
        private Engine engine;

        public Bus getBus() {
            return bus;
        }

        public void setBus(final Bus bus) {
            this.bus = bus;
        }

        public Car getCar() {
            return car;
        }

        public void setCar(final Car car) {
            this.car = car;
        }

        public Engine getEngine() {
            return engine;
        }

        public void setEngine(final Engine engine) {
            this.engine = engine;
        }
    }

    private static class Car {
        private Engine engine;

        public void setEngine(final Engine engine) {
            this.engine = engine;
        }

        public Engine getEngine() {
            return engine;
        }
    }

    private static class Bus {
        private Engine engine;
    }

    private static class Engine {
        private final String reference = "125-458-856";

        public String getReference() {
            return reference;
        }
    }

}