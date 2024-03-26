package lu.mms.common.quality.assets.condition;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;

import java.util.Map;
import java.util.Optional;

import static lu.mms.common.quality.junit.platform.SpiConfiguration.TEST_EXECUTION_RESULTS;

/**
 * This extension will disable the test class test method if any of previously executed test method (in the same test
 * plan failed). This behaviour will prevent tests executed in a given {@linkplain Order @Order} to not execute
 * coming next tests method in case of any failure.
 */
public class DisableTestMethodOnFailureExtension implements ExecutionCondition, AfterAllCallback {

    private static final String DEFAULT_DISABLED_REASON = "The previous test case failed.";
    private static final String TEST_DISABLED_REASON_TEMPLATE = "The test [%s] is disabled because the test [%s] "
            + "didn't succeed. Cause: %s";
    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
            "@Disabled is not present");

    @Override
    public void afterAll(final ExtensionContext context) {
        TEST_EXECUTION_RESULTS.remove(context.getRequiredTestClass().getName());
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        final Optional<Map.Entry<String, TestExecutionResult>> brokenEntry = TEST_EXECUTION_RESULTS
                .entrySet()
                .stream().filter(entry -> context.getRequiredTestClass().getName().equals(entry.getKey()))
                .flatMap(entry -> entry.getValue().entrySet().stream())
                // Keeping the entry which is not successful
                .filter(entry -> entry.getValue().getStatus() != TestExecutionResult.Status.SUCCESSFUL)
                .findFirst();

        if (brokenEntry.isEmpty()) {
            return ENABLED;
        }

        // Retrieve the actual test fully qualified name
        final String element = context.getElement()
                .map(Object::toString)
                .orElse("-");

        // Compute the disabled reason
        final String disabledReason = brokenEntry
                .map(entry -> {
                    final String failedTestName = entry.getKey();
                    final String reason = entry.getValue().toString();
                    return String.format(TEST_DISABLED_REASON_TEMPLATE, element, failedTestName, reason);
                })
                .orElse(DEFAULT_DISABLED_REASON);

        // Disable the test
        return ConditionEvaluationResult.disabled(disabledReason);
    }
}
