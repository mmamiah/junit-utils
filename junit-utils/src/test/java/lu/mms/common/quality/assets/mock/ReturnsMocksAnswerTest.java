package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.emptyArray;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReturnsMocksAnswerTest {

    private ReturnsMocksAnswer sut;

    @Mock
    private Person personMock;

    @Mock(name = "mercedes")
    private Car mercedesMock;

    @Mock(name = "audi")
    private Car audiMock;

    private InternalMocksContext context;

    @BeforeEach
    private void init() {
        context = InternalMocksContext.newEmptyContext();
    }

    @Test
    void shouldReturnFreshMockWhenTargetMockNotFound() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(personMock));
        sut = new ReturnsMocksAnswer(context);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
                                            Person.class, personMock, "getMercedes");

        // Act
        final Object mock = sut.answer(invocation);

        // Assert
        assertThat(mock, notNullValue());
        assertThat(mock, IsNot.not(mercedesMock));
    }

    @Test
    void shouldReturnFreshMockWhenRequestedMockNotInTheContext() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(mercedesMock, personMock));
        sut = new ReturnsMocksAnswer(context);

        final Person notInContext = mock(Person.class);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
                                            Person.class, notInContext, "getMercedes");

        // Act
        final Object mock = sut.answer(invocation);

        // Assert
        assertThat(mock, notNullValue());
        assertThat(mock, IsNot.not(mercedesMock));
    }

    @Test
    void shouldReturnMockWhenSameMockRequested() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(mercedesMock, personMock));
        sut = new ReturnsMocksAnswer(context);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
                                            Person.class, personMock, "getMercedes");

        // Act
        final Object mock = sut.answer(invocation);

        // Assert
        assertThat(mock, equalTo(mercedesMock));
    }

    @Test
    void shouldReturnMockWhenMockRequestedAndMultipleMatch() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(mercedesMock, personMock, audiMock));
        sut = new ReturnsMocksAnswer(context);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
            Person.class, personMock, "getMercedes");

        // Act
        final Object mock = sut.answer(invocation);

        // Assert
        assertThat(mock, equalTo(mercedesMock));
    }

    @Test
    void shouldReturnMockWhenKnownMockCollectionRequested() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(mercedesMock, personMock, audiMock));
        sut = new ReturnsMocksAnswer(context);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
            Person.class, personMock, "getCars");

        // Act
        final Object mock = sut.answer(invocation);

        // Assert
        assertThat(mock, instanceOf(Iterable.class));
        final Iterable<Car> iterableOfMocks = (Iterable<Car>) mock;
        assertThat(iterableOfMocks, hasItem(mercedesMock));
    }

    @Test
    void shouldReturnMockWhenUnknownMockCollectionRequested() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(mercedesMock, personMock, audiMock));
        sut = new ReturnsMocksAnswer(context);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
            Person.class, personMock, "getCarsNames");

        // Act
        final Object mock = sut.answer(invocation);

        // Assert
        assertThat(mock, instanceOf(Iterable.class));
        final Iterable<Car> iterableOfMocks = (Iterable<Car>) mock;
        assertThat(iterableOfMocks, emptyIterable());
    }

    @Test
    void shouldReturnMockWhenUnknownMockArrayRequested() throws Throwable {
        // Arrange
        context.mergeMocks(List.of(mercedesMock, personMock, audiMock));
        sut = new ReturnsMocksAnswer(context);
        final InvocationOnMock invocation = new ReturnMocksInvocationOnMock(
            Person.class, personMock, "getCarsAge");

        // Act
        final Object[] arrayOfMocks = (Object[]) sut.answer(invocation);

        // Assert
        assertThat(arrayOfMocks, emptyArray());
    }

    static class Person {
        private Car mercedes;

        public Car getMercedes() {
            return mercedes;
        }

        public List<Car> getCars() {
            return List.of(mercedes);
        }

        public Set<String> getCarsNames() {
            return null;
        }

        public Integer[] getCarsAge() {
            return null;
        }
    }

    static class Car {
        private String brand;

        public String getBrand() {
            return brand;
        }
    }

    static class ReturnMocksInvocationOnMock implements InvocationOnMock {

        private Object mock;
        private Method method;

        ReturnMocksInvocationOnMock(final Class<?> klass, final Object mock, final String methodName) {
            this.mock = mock;
            method = ReflectionUtils.findMethod(klass, methodName);
        }

        @Override
        public Object getMock() {
            return mock;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return new Object[0];
        }

        @Override
        public <T> T getArgument(final int i) {
            return null;
        }

        @Override
        public <T> T getArgument(final int i, final Class<T> aClass) {
            return null;
        }

        @Override
        public Object callRealMethod() {
            return null;
        }
    }

}
