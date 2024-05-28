package lu.mms.common.quality.userguide.bdd;

import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import lu.mms.common.quality.assets.bdd.source.EnumArgument;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

/**
 * Example to illustrate how the {@link EnumArgument} works, with {@link EnumArgument#include}.
 */
// tag::example[]
@EnumArgument(
        value = GenericSize.class,
        include = {"L", "XL", "XXL"}
)
class EnumArgumentIncludeExampleTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void andThenCustomerHaveALargeSuitSize(final GenericSize value) {
        // Then
        assertThat(value, notNullValue());
        assertThat(value, oneOf(GenericSize.L, GenericSize.XL, GenericSize.XXL));
    }

    void whenWeCalculateCitizenTaxes() {
        // When
    }

    void givenTheCitizenCars() {
        // Given
    }

}
// end::example[]
