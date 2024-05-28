package lu.mms.common.quality.junit.assets.mockvalue.commons;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * This class 'visit' the target object, scan it to find the attributes annotated with [@Value] and initialize then
 * with default value if applicable.
 */
public final class ValueAnnotationVisitor implements MockValueVisitor {

    /**
     * @return The new visitor object
     */
    public static ValueAnnotationVisitor newVisitor() {
        return new ValueAnnotationVisitor();
    }

    @Override
    public void accept(final Object sut) {
        if (sut == null) {
            return;
        }
        findAnnotatedFields(sut.getClass(), Value.class).parallelStream()
            .filter(atValueField -> atValueField.getAnnotation(Value.class) != null)
            .map(atValueField ->
                Pair.of(
                    atValueField,
                    BASIC_VALUE_PATTERN.matcher(atValueField.getAnnotation(Value.class).value())
                )
            )
            // Keep the valid fields only
            .filter(pair -> pair.getValue().matches())
            .forEach(pair -> {
                if (StringUtils.isBlank(pair.getValue().group(2))) {
                    // Skip the value if it is blank
                    return;
                }
                Object objValue = pair.getValue().group(2);
                if (pair.getKey().getType().isArray()) {
                    objValue = pair.getValue().group(2).split(",");
                }
                final Object value = new ModelMapper().map(objValue, pair.getKey().getType());

                // No mater whether this field was initialized before or not, if this 'visitor' is called, we need to
                // re-apply the field defaukting.
                ReflectionTestUtils.setField(sut, pair.getKey().getName(), value);
            });
    }

}
