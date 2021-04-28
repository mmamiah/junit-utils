package lu.mms.common.quality.assets.mock.context;

import org.hamcrest.core.CombinableMatcher;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIn.oneOf;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.CombinableMatcher.either;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

@ExtendWith({MockitoExtension.class, MocksContextParameterResolver.class})
class InternalMocksContextTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalMocksContextTest.class);

    private static final String PHONE_MOCK_NAME = "iPhone 4S";

    private InternalMocksContext sut;

    @Mock
    private Phone phoneMock;

    @Mock(name = PHONE_MOCK_NAME)
    private Phone iphoneMock;

    @Mock
    private Charger chargerMock;

    @BeforeEach
    private void init(final TestInfo testInfo) {
        final String methodName = testInfo.getTestMethod().map(Method::getName).orElse(null);
        sut = InternalMocksContext.newContext(LOGGER, getClass(), this, methodName);
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

    @Test
    void shouldFindMockByNameContextWhenSimpleMock() {
        // Arrange

        // act
        final Object mock = sut.getMockByName("phoneMock");

        // Assert
        assertThat(mock, equalTo(phoneMock));
    }

    @Test
    void shouldNotFindMockByNameContextWhenSearchingByFieldNameAndMockNameSpecifiedInAnnotation() {
        // Arrange

        // act
        final Object mock = sut.getMockByName("iphoneMock");

        // Assert
        assertThat(mock, nullValue());
    }

    @Test
    void shouldFindMockByNameContextWhenMockNameIsFieldName() {
        // Arrange

        // act
        final Object mock = sut.getMockByName(PHONE_MOCK_NAME);

        // Assert
        assertThat(mock, equalTo(iphoneMock));
    }

    @ParameterizedTest
    @ValueSource(classes = {Phone.class, Charger.class})
    <T extends Device> void shouldFindTheMockByClass(final Class<T> mockClass) {
        // Arrange

        // act
        final Set<T> mocks = sut.findMockByClass(mockClass);

        // Assert
        assertThat(mocks, either(hasSize(1)).or(hasSize(2)));
        assertThat(mocks, CombinableMatcher.<Iterable<T>>either(containsInAnyOrder(phoneMock, iphoneMock))
                .or(hasItem((T) chargerMock))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"iphoneMock", "phoneMock", "chargerMock"})
    void shouldFindMockByField(final String fieldName) throws NoSuchFieldException {
        // Arrange
        final Field field = this.getClass().getDeclaredField(fieldName);

        // act
        final Object mock = sut.findMockByField(field);

        // Assert
        assertThat(mock, oneOf(phoneMock, iphoneMock, chargerMock));
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
