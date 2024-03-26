package lu.mms.common.quality.assets.condition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DisableTestMethodOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
class DisableTestMethodOnFailureExtensionNestedTest {

    @Test
    @Order(1)
    void givenCandidateIsEntryLevel() {
        // GIVEN
    }

    @Test
    @DisplayName("When the test case fails")
    @Order(2)
    void whenTheTestCaseFails() {
        // WHEN
        Assertions.fail("Example of failure.");
    }

    @Test
    @Order(3)
    void thenDoNotExecuteAnyOtherTestCase() {
        // THEN
    }

    @ExtendWith(DisableTestMethodOnFailureExtension.class)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class AnotherDisableTestMethodOnFailureExtensionTest {

        @Test
        @Order(1)
        void givenCandidateIsEntryLevel() {
            // GIVEN
        }

        @Test
        @DisplayName("When the test case fails")
        @Order(2)
        void whenTheTestCaseFails() {
            // WHEN
//            Assertions.fail("Example of failure.");
        }

        @Test
        @Order(3)
        void thenDoNotExecuteAnyOtherTestCase() {
            // THEN
        }

//        @ExtendWith(DisableOnFailureExtension.class)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        @Nested
        class AnotherOneDisableTestMethodOnFailureExtensionTest {

            @Test
            @Order(1)
            void givenCandidateIsEntryLevel() {
                // GIVEN
            }

            @Test
            @DisplayName("When the test case fails")
            @Order(2)
            void whenTheTestCaseFails() {
                // WHEN
                Assertions.fail("Example of failure.");
            }

            @Test
            @Order(3)
            void thenDoNotExecuteAnyOtherTestCase() {
                // THEN
            }

        }

    }

    @ExtendWith(DisableTestMethodOnFailureExtension.class)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class AnotherTwoDisableTestMethodOnFailureExtensionTest {

        @Test
        @Order(1)
        void givenCandidateIsEntryLevel() {
            // GIVEN
        }

        @Test
        @DisplayName("When the test case fails")
        @Order(2)
        void whenTheTestCaseFails() {
            // WHEN
//            Assertions.fail("Example of failure.");
        }

        @Test
        @Order(3)
        void thenDoNotExecuteAnyOtherTestCase() {
            // THEN
        }

    }

}
