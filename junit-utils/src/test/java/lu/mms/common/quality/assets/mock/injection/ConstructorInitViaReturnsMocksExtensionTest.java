package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS and #ExtendWithTestUtils.answer is false.
 */
@ExtendWithTestUtils
class ConstructorInitViaReturnsMocksExtensionTest {

    @InjectMocks
    private District sut;

    @Mock(name = "squareBuilding", answer = Answers.RETURNS_MOCKS)
    private Building buildingOneMock;

    @Mock(name = "greenBuilding", answer = Answers.RETURNS_MOCKS)
    private Building buildingTwoMock;

    @Mock
    private Owner ownerMock;

    @Test
    void shouldInjectCorrectMockInSut() {
        // Arrange
        assumeTrue(sut.getMayor() != null);

        // Act
        final Owner mayor = sut.getMayor();

        // Assert
        assertThat(mayor, equalTo(ownerMock));
    }

    @Test
    void shouldInstantiateSutWithExactConstructorArgumentWhenArgumentWithSameType() {
        // Arrange

        // Act
        final Building greenBuilding = sut.getGreenBuilding();
        final Building squareBuilding = sut.getSquareBuilding();

        // Assert
        assertThat(squareBuilding, allOf(notNullValue(), equalTo(buildingOneMock)));
        assertThat(greenBuilding, allOf(notNullValue(), equalTo(buildingTwoMock)));
    }


    // Test case inline model

    private static class District {
        private final Owner mayor;
        private final Building squareBuilding;
        private final Building greenBuilding;

        District() {
            mayor = null;
            this.squareBuilding = null;
            this.greenBuilding = null;
        }

        District(final String mayorOne) {
            mayor = null;
            this.squareBuilding = null;
            this.greenBuilding = null;
        }

        District(final Building building) {
            this.mayor = building.getOwner();
            this.squareBuilding = building;
            this.greenBuilding = building;
        }

        District(final Building squareBuilding, final Building greenBuilding) {
            this.mayor = squareBuilding.getOwner();
            this.squareBuilding = squareBuilding;
            this.greenBuilding = greenBuilding;
        }

        Owner getMayor() {
            return mayor;
        }

        Building getSquareBuilding() {
            return squareBuilding;
        }

        Building getGreenBuilding() {
            return greenBuilding;
        }
    }

    private static class Building {
        private Owner owner;

        Owner getOwner() {
            return owner;
        }
    }

    private static class Owner {
        private String name;

        String getName() {
            return name;
        }
    }

}
