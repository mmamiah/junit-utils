package lu.mms.common.quality.userguide.fixture;

import lu.mms.common.quality.assets.fixture.Fixture;
import lu.mms.common.quality.assets.fixture.FixtureExtension;
import lu.mms.common.quality.userguide.models.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Example to illustrate how @{@link Fixture} works.
 */
// tag::example[]
@ExtendWith({MockitoExtension.class, FixtureExtension.class})
class FixtureExample1Test {

    @Mock
    private Report reportMock;

    @Fixture
    private FixtureFileExample fixture;

    @Test
    void shouldHaveAConfiguredFixtureFile() {
        // Arrange
        fixture.givenCustomerIdIsTwenty();

        // Act
        final Integer id = reportMock.getCustomerId();

        // Assert
        assertThat(id, equalTo(20));
    }
}
// end::example[]
