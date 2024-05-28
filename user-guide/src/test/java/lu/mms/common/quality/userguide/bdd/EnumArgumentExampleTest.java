package lu.mms.common.quality.userguide.bdd;

import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import lu.mms.common.quality.assets.bdd.source.EnumArgument;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Example to illustrate how the {@link EnumArgument} works.
 */
// tag::example[]
@EnumArgument(GenericSize.class)
class EnumArgumentExampleTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void givenCitizenClothesSize(final GenericSize value) {
        // Given
        assertThat(value, notNullValue());
    }

    void whenWeCalculateCitizenTaxes() {
        // When
    }

    void thenTheTaxAreRatedForCategoryA() {
        // Then
    }

}
// end::example[]
