package lu.mms.common.quality.userguide.bdd;

import lu.mms.common.quality.assets.bdd.ScenarioNameGenerator;
import lu.mms.common.quality.assets.bdd.SpecificationDrivenOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Example to illustrate how the {@link SpecificationDrivenOrderer} works.
 */
// tag::example[]
@TestMethodOrder(SpecificationDrivenOrderer.class)
@DisplayNameGeneration(ScenarioNameGenerator.class)
class SpecificationDrivenOrdererExampleTest {

    @Test
    void andGivenASimpleCitizen() {/*Given*/}

    @Test
    void andThenCitizenHouseWillNotBeCharged() {/*Then*/}

    @Test
    void thenTheTaxAreRatedForCategoryA() {/*Then*/}

    @Test
    void givenTheCitizenFamily() {/*Given*/}

    @Test
    void whenWeCalculateCitizenTaxes() {/*When*/}

    @Test
    void givenTheCitizenCarsAndHouse() {/*Given*/}

    @Test
    void givenTheCitizenCurrentJobAndPosition() {/*Given */}

    @Test
    @DisplayName("Given the citizen pets")
    void citizen_has_pets() {/* Given*/}

}
// end::example[]
