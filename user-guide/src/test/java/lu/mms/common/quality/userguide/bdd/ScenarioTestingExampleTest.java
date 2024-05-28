package lu.mms.common.quality.userguide.bdd;

import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

/**
 * Example to illustrate how the {@link ScenarioTesting} works.
 */
// tag::example[]
class ScenarioTestingExampleTest extends ScenarioTesting {

    @DisplayName("Scenario: Feature #1")
    @TestFactory
    public Stream<DynamicNode> myScenariosTestFactory() {
        return super.scenariosTestFactoryTemplate();
    }

    void givenNewCustomer() {
        // Given
    }

    void whenCustomerAppliesForNewAccount() {
        // When
    }

    void thenAccountIsCreated() {
        // Then
    }

}
// end::example[]
