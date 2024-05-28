package lu.mms.common.quality.assets.mockvalue;

import lu.mms.common.quality.JunitUtilsPreconditionException;
import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mockvalue.commons.MockValueDefaultVisitor;
import lu.mms.common.quality.assets.mockvalue.commons.TestMethodMockValuesVisitor;
import lu.mms.common.quality.assets.mockvalue.commons.ValueAnnotationVisitor;
import lu.mms.common.quality.assets.spring.context.SpringContextRunnerExtension;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * The Extension for {@link MockValue} annotation. <br>
 * This extension will get all the "@MockValue" annotated properties within the test class instance,
 * and use them to initialize the related "@Value" annotated properties, in the test class
 * instance System Under Test ("@InjectMocks").
 * <b>impl note:</b> {@link Value} using Spring Expression Language are not supported
 */
public class MockValueExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextRunnerExtension.class);
    private static final String NULL_MOCKVALUE_FIELD_ERROR_TMPL = "The field '%s' (annotated with @MockValue) "
            + "is 'NULL'. Please remove the field if it is not used, or set a default value.";

    @Override
    public void beforeEach(final ExtensionContext context) {
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);
        mocksContext.visit(this);
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }

    /**
     * Initialize the @Value (subject under test) and the @MockValue (test instance) for a
     * given test method (test case).
     * @param context The test instance mocks context
     */
    @Override
    public void accept(final InternalMocksContext context) {
        final Object testInstance = context.getTestInstance();
        final String testMethodName = context.getTestMethodName();

        final Pair<Field, Object> fieldPair = findAnnotatedFields(context.getTestClass(), InjectMocks.class)
                .stream()
                .findFirst()
                .map(field -> Pair.of(field, ReflectionTestUtils.getField(testInstance, field.getName())))
                .orElse(Pair.of(null, null));

        // [#1] If a @Value is defined with default value, it should be initialized with that default value
        ValueAnnotationVisitor.newVisitor().accept(fieldPair.getValue());

        // [#2] If we declared a @MockValue with default value, the related default value will be injected
        // to the SUT, ignoring the previous @Value definition.
        MockValueDefaultVisitor.newVisitor(testInstance).accept(fieldPair.getValue());

        // [#3] If we declared a @MockValue with an assigned value, then it will be injected to the SUT,
        // ignoring the previous @Value definition.
        TestMethodMockValuesVisitor.newVisitor(testInstance, testMethodName).accept(fieldPair.getValue());

        // [#4] Throw exception if any @MockValue field is null
        findAnnotatedFields(testInstance.getClass(), MockValue.class).forEach(field -> {
            if (ReflectionTestUtils.getField(testInstance, field.getName()) == null) {
                final String msg = String.format(NULL_MOCKVALUE_FIELD_ERROR_TMPL, field.getName());
                throw new JunitUtilsPreconditionException(msg);
            }
        });
    }

}
