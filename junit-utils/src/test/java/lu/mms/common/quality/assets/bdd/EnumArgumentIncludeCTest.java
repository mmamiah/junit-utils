package lu.mms.common.quality.assets.bdd;

import lu.mms.common.quality.assets.bdd.source.EnumArgument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

@EnumArgument(
        value = GenericSize.class,
        include = {"L", "XL", "XXL"}
)
class EnumArgumentIncludeCTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void andGivenCitizenSize(final Enum<?> value) {
        // Given
        assertThat(value, notNullValue());
        assertThat(value, oneOf(GenericSize.L, GenericSize.XL, GenericSize.XXL));
    }

    void andThenCitizenHouseWillNotBeCharged(final GenericSize value) {
        // Then
        assertThat(value, notNullValue());
        assertThat(value, oneOf(GenericSize.L, GenericSize.XL, GenericSize.XXL));
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
