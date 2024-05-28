package lu.mms.common.quality.assets.mockvalue.commons;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lu.mms.common.quality.assets.mockvalue.commons.MockValueVisitor.BASIC_VALUE_PATTERN;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * This class is responsible of the proper orchestration of the {@link Value} initialization templates, applied to the
 * tst instance members annotated with the provided annotation class.
 */
public final class MockValueContainerInjectionTemplate implements Consumer<Class<? extends Annotation>> {

    private final Object testInstance;
    private final String testMethodName;

    private MockValueContainerInjectionTemplate(final Object testInstance, final String testMethodName) {
        this.testInstance = testInstance;
        this.testMethodName = testMethodName;
    }

    /**
     * @param testInstance  The test object instance
     * @param testMethodName    The test method name
     * @return  The mock value injection template
     */
    public static MockValueContainerInjectionTemplate newTemplate(final Object testInstance,
                                                                  final String testMethodName) {
        return new MockValueContainerInjectionTemplate(testInstance, testMethodName);
    }

    @Override
    public void accept(final Class<? extends Annotation> annotationClass) {
        final Map<String, Field> mockValueFields = findAnnotatedFields(testInstance.getClass(), MockValue.class)
                .stream()
                .flatMap(field ->
                        Stream.of(field.getAnnotation(MockValue.class).value())
                                .map(value -> Pair.of(field, BASIC_VALUE_PATTERN.matcher(value)))
                )
                .filter(pair -> pair.getValue().matches())
                .collect(Collectors.toMap(pair -> pair.getValue().group(1), Pair::getKey, (o, v) -> o));

        // inject the test instance @MockValue to the @Spy field @Value annotated
        findAnnotatedFields(testInstance.getClass(), annotationClass)
                .stream()
                .map(field -> Pair.of(field, ReflectionTestUtils.getField(testInstance, field.getName())))
                .forEach(pair -> {
                    // [#1] If a @Value is defined with default value, it should be initialized with that default value
                    ValueAnnotationVisitor.newVisitor().accept(pair.getValue());

                    // [#2] Inject relevant value to target field
                    injectMockValueToSpyFields(mockValueFields, pair);

                    // [#3] If we declared a @MockValue with an assigned value, then it will be injected to the SUT,
                    // ignoring the previous @Value definition.
                    TestMethodMockValuesVisitor.newVisitor(testInstance, testMethodName).accept(pair.getValue());
                });

    }

    private void injectMockValueToSpyFields(final Map<String, Field> mockValueFields, final Pair<Field, Object> pair) {
        findAnnotatedFields(pair.getValue().getClass(), Value.class).stream()
                .flatMap(field ->
                        Stream.of(field.getAnnotation(Value.class).value())
                                .map(value -> Pair.of(field, BASIC_VALUE_PATTERN.matcher(value)))
                )
                .filter(valuePair -> valuePair.getValue().matches())
                .filter(valuePair -> mockValueFields.containsKey(valuePair.getValue().group(1)))
                .forEach(valuePair -> {
                    // get the value to inject
                    final Field source = mockValueFields.get(valuePair.getValue().group(1));
                    final Object sourceValue = ReflectionTestUtils.getField(testInstance, source.getName());

                    // inject the value to the @Spy field
                    ReflectionTestUtils.setField(pair.getValue(), valuePair.getKey().getName(), sourceValue);
                });
    }

}
