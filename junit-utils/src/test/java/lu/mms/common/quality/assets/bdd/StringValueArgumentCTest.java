package lu.mms.common.quality.assets.bdd;

import lu.mms.common.quality.assets.bdd.source.ValueArgument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.core.IsNull.notNullValue;

@ValueArgument({"A", "B", "C"})
class StringValueArgumentCTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void andGivenCitizenSize(final Object value) {
        // Given
        assertThat(value, notNullValue());
        assertThat(value, oneOf("A", "B", "C"));
    }

    void andThenCitizenHouseWillNotBeCharged(final String value) {
        // Then
        assertThat(value, notNullValue());
        assertThat(value, oneOf("A", "B", "C"));
    }

    void thenTheTaxAreRatedForCategoryA() {
        // Then
    }

    void givenTheCitizenFamily() {
        // Given
    }

    void whenWeCalculateCitizenTaxes() {
        // When
    }

    void givenTheCitizenCarsAndHouse() {
        // Given
    }

    @DisplayName("and  given The Citizen Current   Job And     Position")
    void givenTheCitizenCurrentJobAndPosition() {
        // Given
    }

    @DisplayName("Given the citizen pets")
    void citizen_has_pets() {
        // Given
    }

}
