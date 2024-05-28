package lu.mms.common.quality.assets.mock.context;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

@ExtendWith({MocksContextParameterResolver.class, MockitoExtension.class})
class ResolvedMocksContextTest {

    private static final String PHONE_MOCK_NAME = "iPhone 4S";

    @Mock
    private Phone phoneMock;

    @Mock(name = PHONE_MOCK_NAME)
    private Phone iphoneMock;

    @Mock
    private Charger chargerMock;

    @Test
    void shouldProperlyInitContextFor(final InternalMocksContext context) {
        // Arrange
        assumeTrue(context != null);

        // Act / Assert
        assertThat(context.getTestInstance(), equalTo(this));
        assertThat(context.contains(phoneMock), equalTo(true));
        assertThat(context.contains(iphoneMock), equalTo(true));
        assertThat(context.contains(chargerMock), equalTo(true));

        assertThat(context.getMockByName(PHONE_MOCK_NAME), equalTo(iphoneMock));
        assertThat(context.findMockByClass(Charger.class), hasItem(chargerMock));

    }

    @Test
    void shouldConfirmTestContextProperlyInitialized(final InternalMocksContext context) {
        // Arrange
        assumeTrue(context != null);

        // Act / Assert
        assertThat(context.contains(chargerMock), Is.is(true));
        assertThat(context.contains(phoneMock), Is.is(true));
        assertThat(context.contains(iphoneMock), Is.is(true));

        assertThat(context.contains(Charger.class), Is.is(true));
        assertThat(context.contains(Phone.class), Is.is(true));

        assertThat(context.contains(String.class), Is.is(false));

        assertThat(context.contains(String.class), Is.is(false));
    }

    @Test
    void shouldConfirmEmptyContextWhenCreatingEmptyContext() {
        // Act
        final InternalMocksContext context = InternalMocksContext.newEmptyContext();

        // Assert
        assertThat(context, notNullValue());
        assertThat(context.getSpyFields(), allOf(
            notNullValue(),
            emptyIterable()
        ));
        assertThat(context.getMocks(), allOf(
            notNullValue(),
            emptyIterable()
        ));
        assertThat(context.getMockClasses(), allOf(
            notNullValue(),
            emptyIterable()
        ));
    }

    @ParameterizedTest
    @MethodSource("findByClassProvider")
    void shouldConfirmAssignableClassesWhenSearchingWithCommonClass(final Collection<Object> feed,
                                                                    final Class<Object> searchClass,
                                                                    final Collection<Class<?>> expectedResult) {
        // Arrange
        final InternalMocksContext context = InternalMocksContext.newEmptyContext();
        context.mergeMocks(feed);

        // Act
        final List<Object> result = context.findAssignableMocks(searchClass);

        // Assert
        assertThat(result, hasItems(expectedResult.toArray()));
    }

    @ParameterizedTest
    @MethodSource("contextContainsProvider")
    void shouldConfirmContextContainsMockWhenMockAddedToContext(final Collection<Object> feed, final Object searchItem,
                                                                final boolean expectedResult) {
        // Act
        final InternalMocksContext context = InternalMocksContext.newEmptyContext();
        context.mergeMocks(feed);

        // Assert
        assertThat(context.contains(searchItem), equalTo(expectedResult));
        Class<?> realClass = searchItem.getClass();
        if (MockUtil.isMock(searchItem)) {
            realClass = MockUtil.getMockSettings(searchItem).getTypeToMock();
        }
        assertThat(context.contains(realClass), equalTo(expectedResult));
    }

    private static Stream<Arguments> findByClassProvider() {
        final Phone phoneOne = mock(Phone.class, "phoneOne");
        final Phone phoneTwo = mock(Phone.class, "phoneTwo");
        final Charger specialCharger = mock(Charger.class, "specialCharger");
        final List<Device> mocks = List.of(phoneOne, phoneTwo, specialCharger);

        return Stream.of(
            Arguments.of(mocks, Device.class, mocks),
            Arguments.of(mocks, Phone.class, List.of(phoneOne, phoneTwo)),
            Arguments.of(mocks, Charger.class, List.of(specialCharger)),
            Arguments.of(mocks, String.class, List.of())
        );
    }

    private static Stream<Arguments> contextContainsProvider() {
        final Phone phoneOne = mock(Phone.class, "phoneOne");
        final Phone phoneTwo = mock(Phone.class, "phoneTwo");
        final Charger specialCharger = mock(Charger.class, "specialCharger");
        final List<Device> mocks = List.of(phoneOne, phoneTwo, specialCharger);

        return Stream.of(
            Arguments.of(mocks, phoneOne, true),
            Arguments.of(mocks, phoneTwo, true),
            Arguments.of(mocks, specialCharger, true),
            Arguments.of(mocks, PHONE_MOCK_NAME, false)
        );
    }

    private interface Device {
        // empty interface
    }

    private static class Phone implements Device {
        // empty class
    }

    private static class Charger implements Device {
        // empty class
    }

}
