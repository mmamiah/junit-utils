package lu.mms.common.quality.assets.mockvalue.commons;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Stream;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.MODEL_MAPPER;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * The Test method (test case) MockValue visitor. <br>
 * This class visit the test instance, and initialize the field annotated with @${@link MockValue}, linked to a given
 * testcase, i.e ${@link MockValue#testcase()} is not empty.
 */
public final class TestMethodMockValuesVisitor implements MockValueVisitor {

    private final Object testInstance;
    private final String testMethodName;

    private TestMethodMockValuesVisitor(final Object testInstance, final String testMethodName) {
        this.testMethodName = testMethodName;
        this.testInstance = testInstance;
    }

    public static TestMethodMockValuesVisitor newVisitor(final Object testInstance, final String testMethodName) {
        return new TestMethodMockValuesVisitor(testInstance, testMethodName);
    }

    @Override
    public void accept(final Object sut) {
        // collect the fields have the test method name specified in their config
        findAnnotatedFields(testInstance.getClass(), MockValue.class)
            .stream()
            // Keep the fields which annotation mention the actual test method
            .filter(field -> ArrayUtils.contains(field.getAnnotation(MockValue.class).testcase(), this.testMethodName))
            .flatMap(field ->
                Stream.of(field.getAnnotation(MockValue.class).value())
                    .map(value -> Pair.of(field, BASIC_VALUE_PATTERN.matcher(value)))
            )
            // Keep matches only
            .filter(pair -> pair.getValue().matches())
            // Inject the value in the corresponding fields
            .forEach(pair -> {
                // collect the fields having the request property settings
                collectValueFields(sut, pair.getValue().group(1)).forEach(field -> {
                    // get the test instance defined value
                    Object targetValue = ReflectionTestUtils.getField(testInstance, pair.getKey().getName());
                    if (targetValue == null) {
                        return;
                    } else if (field.getType().isArray()) {
                        // map to the correct type
                        targetValue = Stream.of(targetValue).toArray();
                        targetValue = MODEL_MAPPER.map(targetValue, field.getType());
                    }
                    ReflectionTestUtils.setField(sut, field.getName(), targetValue);
                });
            });
    }

}
