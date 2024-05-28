package lu.mms.common.quality.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.oneOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

@ExtendWith(MockitoExtension.class)
class TargetInstanceMocksContextTest {

    private static final String PHONE_MOCK_NAME = "iPhone 4S";

    private TargetInstanceMocksContext sut;

    @Mock
    private Phone phoneMock;

    @Mock(name = PHONE_MOCK_NAME)
    private Phone iphoneMock;

    @Mock
    private Charger chargerMock;

    @BeforeEach
    void init() {
        sut = TargetInstanceMocksContext.newContext(this);
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
    void shouldFindTheMockByClass(final Class<?> mockClass) {
        // Arrange

        // act
        final Object mock = sut.findMockByClass(mockClass);

        // Assert
        assertThat(mock, oneOf(phoneMock, iphoneMock, chargerMock));
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

    private static class Phone {
        // empty class
    }

    private static class Charger {
        // empty class
    }

}