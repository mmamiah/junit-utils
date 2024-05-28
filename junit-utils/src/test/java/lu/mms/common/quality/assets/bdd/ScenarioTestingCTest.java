package lu.mms.common.quality.assets.bdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class ScenarioTestingCTest extends ScenarioTesting{

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
