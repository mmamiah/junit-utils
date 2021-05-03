package lu.mms.common.quality.assets.mockvalue.commons;

import lu.mms.common.quality.assets.mockvalue.MockValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.MODEL_MAPPER;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * The default MockValue visitor. <br>
 * This class visit the test instance, and initialize the field annotated with @${@link MockValue}, not linked to any
 * testcase, i.e ${@link MockValue#testcase()} is empty.
 */
public final class MockValueDefaultVisitor implements MockValueVisitor {

    private final Object testInstance;

    private MockValueDefaultVisitor(final Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * @param testInstance The test instance
     * @return the mock value default visitor object
     */
    public static MockValueDefaultVisitor newVisitor(final Object testInstance) {
        return new MockValueDefaultVisitor(testInstance);
    }

    @Override
    public void accept(final Object sut) {
        findAnnotatedFields(testInstance.getClass(), MockValue.class)
            .stream()
            .flatMap(field ->
                    Stream.of(field.getAnnotation(MockValue.class).value())
                            .map(value -> Pair.of(field, BASIC_VALUE_PATTERN.matcher(value)))
            )
            // Keeping the field that are matching only
            .filter(pair -> pair.getValue().matches())
            .forEach(pair -> injectMockValue(sut, pair));

    }

    private void injectMockValue(final Object sut, final Pair<Field, Matcher> pair) {
        final String property = pair.getValue().group(1);
        final Field mockValueField = pair.getKey();

        final List<Field> atValueFields = collectValueFields(sut, property);
        final String defaultValue = pair.getValue().group(2);

        if (atValueFields.isEmpty()) {
            // just init the test instance @MockValue fields
            instantiateAtMockValue(sut, null, mockValueField, defaultValue);
        } else {
            atValueFields.forEach(field -> {
                // [1]:  initialize the @MockValue fields
                final Object value = retrieveValueToInject(sut, field, mockValueField, defaultValue);
                instantiateAtMockValue(sut, field, mockValueField, defaultValue);

                // Keep the non 'testcase' MockValues only
                if (value == null || ArrayUtils.isNotEmpty(mockValueField.getAnnotation(MockValue.class).testcase())) {
                    return;
                }

                // [2]:  Synchronize the test instance @MockValue fields with the SUT.
                final Object valueToInject = mapTargetValue(field.getType(), value);
                ReflectionTestUtils.setField(sut, field.getName(), valueToInject);
            });
        }

    }

    private void instantiateAtMockValue(final Object sut, final Field field, final Field mockValueField,
                                        final String defaultValue) {
        final Object value = retrieveValueToInject(sut, field, mockValueField, defaultValue);

        if (value == null) {
            return;
        }

        // [1]:  initialize the @MockValue fields
        final Object valueToInject = mapTargetValue(mockValueField.getType(), value);
        if (ReflectionTestUtils.getField(testInstance, mockValueField.getName()) == null) {
            ReflectionTestUtils.setField(testInstance, mockValueField.getName(), valueToInject);
        }
    }

    private Object retrieveValueToInject(final Object source, final Field sourceField, final Field mockValueField,
                                         final String defaultValue) {
        Object value;
        // #1st choice: the @MockValue default value
        if (StringUtils.isNotBlank(defaultValue)) {
            value = defaultValue;
        } else {
            // if the @MockValue default value is some how blank, then
            // #2nd choice: the @MockValue assigned value
            value = ReflectionTestUtils.getField(testInstance, mockValueField.getName());
        }

        // if still couldn't find non NULL or non BLANK value, then
        // #3rd choice: the @Value assigned value from previous stage (See ValueAnnotationVisitor).
        if (sourceField != null && (value == null || StringUtils.isBlank(String.valueOf(value)))) {
            value = ReflectionTestUtils.getField(source, sourceField.getName());
        }
        return value;
    }

    private Object mapTargetValue(final Class<?> targetClass, final Object value) {
        if (targetClass == null || Objects.equals(value.getClass(), targetClass)) {
            return value;
        }
        Object valueToInject = value;
        if (ClassUtils.isPrimitiveOrWrapper(targetClass) || (targetClass.equals(String.class))) {
            if (valueToInject.getClass().equals(String.class)) {
                valueToInject = String.valueOf(valueToInject).split(",");
            }
            if (isArrayAndIsNotEmpty(valueToInject)) {
                final Collection<?> values = new ArrayList<>();
                CollectionUtils.mergeArrayIntoCollection(valueToInject, values);
                valueToInject = values.stream()
                        .map(String::valueOf)
                        .reduce((a, b) -> String.join(", ", a, b))
                        .orElse(StringUtils.EMPTY);
            }

        } else if (targetClass.isArray() && value.getClass().equals(String.class)) {
            valueToInject = String.valueOf(value).split(",");
        }
        if (!Objects.equals(valueToInject.getClass(), targetClass)) {
            valueToInject = MODEL_MAPPER.map(valueToInject, targetClass);
        }
        return valueToInject;
    }

    /**
     * Initialize the <b>targetField</b> with the following value:
     * <ol>
     *     <li>With the source field (source#sourceFieldName) if it is not null</li>
     *     <li>Otherwise, with the <b>annotationDefaultValue</b> if it is not null as well</li>
     * </ol>
     * @param source The the source object containing the wanted value
     * @param sourceFieldName The source field name
     * @param target The target object where to set the selected value
     * @param targetField The target field (in the target object)
     * @param annotationDefaultValue The value to set in case no other value match
     * @return The value injected in the <b>targetField</b>.
     */
    private static Object initMockValueField(final Object source, final String sourceFieldName,
                                             final Object target, final Field targetField,
                                             final Object annotationDefaultValue) {
        // get annotated field value
        Object value = ReflectionTestUtils.getField(source, sourceFieldName);
        value = sanitizeTargetValue(targetField, annotationDefaultValue, value);

        if (value == null) {
            return null;
        }

        if (!(targetField.getType().isArray() && value.getClass().isArray())) {
            if (isArrayAndIsNotEmpty(value)) {
                value = CollectionUtils.arrayToList(value).listIterator().next();
            }
            value = MODEL_MAPPER.map(value, targetField.getType());
        }

        ReflectionTestUtils.setField(target, targetField.getName(), value);
        return value;
    }

    private static Object sanitizeTargetValue(final Field targetField, final Object annotationDefaultValue,
                                              final Object targetValue) {
        Object result = targetValue;
        if (annotationDefaultValue == null) {
            // If no default value was defined then keep the target value AS-IS
            return result;
        }
        if (targetValue == null && StringUtils.isNotBlank(annotationDefaultValue.toString())) {
            // Get the annotation default value
            if (targetField.getType().isArray()) {
                result = String.valueOf(annotationDefaultValue).split(",");
            } else {
                result = annotationDefaultValue;
            }
        }
        return result;
    }

    private static boolean isArrayAndIsNotEmpty(Object object) {
        return object.getClass().isArray() && ArrayUtils.getLength(object) > 0;
    }

}
