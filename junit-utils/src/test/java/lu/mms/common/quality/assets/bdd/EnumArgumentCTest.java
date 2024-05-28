package lu.mms.common.quality.assets.bdd;

import lu.mms.common.quality.assets.bdd.source.EnumArgument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@EnumArgument(GenericSize.class)
class EnumArgumentCTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void andGivenCitizenSize(final Enum<?> value) {
        // Given
        assertThat(value, notNullValue());
    }

    void andThenCitizenHouseWillNotBeCharged(final GenericSize value) {
        // Then
        assertThat(value, notNullValue());
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
