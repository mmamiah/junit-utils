package lu.mms.common.quality.userguide.bdd;

import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import lu.mms.common.quality.assets.bdd.source.ValueArgument;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Example to illustrate how the {@link ValueArgument} works with {@link ValueArgument#value}.
 */
// tag::example[]
@ValueArgument({"A", "B", "C"})
class StringValueArgumentExampleTest extends ScenarioTesting {

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void givenCitizenType(final Object value) {
        // Given
        assertThat(value, notNullValue());
        assertThat(value, oneOf("A", "B", "C"));
    }

    void thenCitizenIsChargedByTemplate(final String value) {
        // Then
        assertThat(value, notNullValue());
        assertThat(value, oneOf("A", "B", "C"));
    }

    void angGivenCitizenTaxCategory() {
        // Given
    }

    void whenCitizenTaxAreRatedForCategoryA() {
        // When
    }

}
// end::example[]
