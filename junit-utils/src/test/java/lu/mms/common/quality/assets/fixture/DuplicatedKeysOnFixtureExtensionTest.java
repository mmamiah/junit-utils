package lu.mms.common.quality.assets.fixture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@ExtendWith({MockitoExtension.class, FixtureExtension.class})
class DuplicatedKeysOnFixtureExtensionTest {

    @Fixture
    private DefaultFixture fixture;

    @Mock(name = "bookOne")
    private BookService redBook;

    @Mock
    private BookService bigBook;

    @Test
    void shouldInitializeMockFixtureWhenSimpleMock() {
        // Arrange

        // Act
        final BookService result = fixture.getBigBook();

        // Assert
        assertThat(result, notNullValue());
        assertThat(result, equalTo(bigBook));
        assertThat(fixture.getRedBook(), nullValue());
    }

    @Test
    void shouldInitializeMockFixtureWhenNamedInAnnotation() {
        // Arrange

        // Act
        final BookService result = fixture.getBookOne();

        // Assert
        assertThat(result, notNullValue());
        assertThat(result, equalTo(redBook));
        assertThat(fixture.getRedBook(), nullValue());
    }
}
