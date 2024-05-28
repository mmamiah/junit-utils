package lu.mms.common.quality.userguide.conditions;

import lu.mms.common.quality.assets.conditions.DisableTestMethodOnFailureExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Example to illustrate how skipped test method in case of failure works.
 */
// tag::example[]
@ExtendWith(DisableTestMethodOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisableTestMethodOnFailureExampleTest {

    @Test
    @Order(1)
    void givenASuccessfulTestCase() {
        // GIVEN
    }

    @Test
    @DisplayName("When This test fail, all other tests should be ignored.")
    @Order(2)
    void whenThisTestCaseFails() {
        // WHEN
        assumeTrue(false, "Example of failure.");
    }

    @Test
    @Order(3)
    void thenDoNotExecuteAnyOtherTestCase() {
        // THEN
    }

}
// end::example[]