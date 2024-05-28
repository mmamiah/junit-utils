package lu.mms.common.quality.assets.conditions;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This extension will disable the tests methods if any of previously executed test method (in the same test
 * plan) failed. This behaviour will prevent to continue with tests execution (for example the one executed in a given
 * {@linkplain Order @Order}, to not execute up coming tests method in case of any failure.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public class DisableTestMethodOnFailureExtension extends JunitUtilsExtension implements ExecutionCondition,
                                                                                        AfterTestExecutionCallback {

    private static final String TEST_DISABLED_REASON_TEMPLATE = "The test [%s] \nis disabled because the test [%s] "
            + "\ndidn't succeed. Cause: %s";
    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
            "@Disabled is not present");

    private String testFailedName = StringUtils.EMPTY;
    private String testFailedReason = StringUtils.EMPTY;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {

        if (StringUtils.isBlank(testFailedName)) {
            return ENABLED;
        }

        // Retrieve the actual test fully qualified name
        final String element = context.getElement()
                .map(Object::toString)
                .orElse("-");

        // Compute the disabled reason
        final String disabledReason = String.format(
                TEST_DISABLED_REASON_TEMPLATE, element, testFailedName, testFailedReason
        );

        // Disable the test
        return ConditionEvaluationResult.disabled(disabledReason);
    }

    @Override
    public void afterTestExecution(final ExtensionContext extensionContext) {
        extensionContext.getExecutionException().ifPresent(exception -> {
            this.testFailedName = extensionContext.getDisplayName();
            this.testFailedReason = exception.getMessage();
        });
    }
}
