package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.MockInjectionExtension;
import lu.mms.common.quality.assets.unittest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testing the Mockito default behavior when #Answers.RETURNS_MOCKS and #UnitTest.answer is false.
 */
@UnitTest
@ExtendWith({MockitoExtension.class, MockInjectionExtension.class})
class ConstructorInitViaReturnsMocksExtensionTest {

    @InjectMocks
    private District sut;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Building buildingMock;

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


    // Test case inline model

    private static class District {
        private final Owner mayor;

        District() {
            mayor = null;
        }

        District(final String mayorOne) {
            mayor = null;
        }

        District(final String mayorOne, final String mayorTwo) {
            mayor = null;
        }

        District(final Building building) {
            this.mayor = building.getOwner();
        }

        Owner getMayor() {
            return mayor;
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
