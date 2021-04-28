package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.unittest.UnitTest;
import lu.mms.common.quality.commons.MockSpyFieldPredicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Answers;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Extension to handle Annotated mocks as per mockito settings.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "3.0.0"
)
public class ReturnsMocksExtension extends JunitUtilsExtension
                                    implements BeforeEachCallback, BeforeTestExecutionCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnsMocksExtension.class);
    private Set<Field> previousMockFields;

    @Override
    public void beforeEach(final ExtensionContext context) {
        final UnitTest unitTest = context.getRequiredTestClass().getDeclaredAnnotation(UnitTest.class);
        if (unitTest != null && !unitTest.returnMocks()) {
            return;
        }

        previousMockFields = ReflectionUtils.getAllFields(
                context.getRequiredTestClass(),
                MockSpyFieldPredicate.newPredicate(context.getRequiredTestInstance())
        );
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);
        mocksContext.visit(this);
    }

    @Override
    public void beforeTestExecution(final ExtensionContext context) {
        final UnitTest unitTest = context.getRequiredTestClass().getDeclaredAnnotation(UnitTest.class);
        if (unitTest != null && !unitTest.returnMocks()) {
            return;
        }

        final Object testInstance = context.getRequiredTestInstance();
        final Set<Field> actualMockFields = ReflectionUtils.getAllFields(
                context.getRequiredTestClass(),
                MockSpyFieldPredicate.newPredicate(testInstance)
        );

        // If the context changed, we need to re-apply the answer again.
        if (!previousMockFields.equals(actualMockFields)) {
            final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);
            mocksContext.refresh();
            mocksContext.visit(this);
        }

    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }

    /**
     * Ensure mocks/spies in the {@link InternalMocksContext} will return declared mocks if needed .
     * @param mocksContext The mocks context
     */
    @Override
    public void accept(final InternalMocksContext mocksContext) {
        final ReturnsMocksAnswer annotatedAnswer = new ReturnsMocksAnswer(mocksContext);

        mocksContext.getMocks().stream()
                .filter(ObjectUtils::allNotNull)
                .map(mock -> MockUtil.getMockSettings(mock).getDefaultAnswer())
                .filter(answer -> Answers.RETURNS_MOCKS == answer)
                .forEach(answer -> ReflectionTestUtils.setField(answer, "implementation", annotatedAnswer));
    }

}
