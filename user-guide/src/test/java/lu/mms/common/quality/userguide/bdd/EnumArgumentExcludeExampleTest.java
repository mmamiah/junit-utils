package lu.mms.common.quality.userguide.bdd;

import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import lu.mms.common.quality.assets.bdd.source.EnumArgument;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

/**
 * Example to illustrate how the {@link EnumArgument} works, with {@link EnumArgument#exclude}.
 */
// tag::example[]
@EnumArgument(
        value = GenericSize.class,
        exclude = {"L", "XL", "XXL"}
)
class EnumArgumentExcludeExampleTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void whenNoOneInCustomerFamilyDoNotHaveALargeSuitSize(final GenericSize value) {
        // When
        assertThat(value, allOf(
                notNullValue(), not(GenericSize.L), not(GenericSize.XL), not(GenericSize.XXL))
        );
    }

    void thenTheTaxAreRatedForCategoryA() {
        // Then
    }

    void givenTheCitizenFamily() {
        // Given
    }

}
// end::example[]
