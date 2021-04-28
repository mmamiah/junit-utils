package lu.mms.common.quality.assets.mockvalue.commons;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

/**
 * The goal of these visitors is to scan the target object and, depending of the context, set the correct value to
 * the [@Value] annotated object.
 */
public interface MockValueVisitor extends Consumer<Object> {

    /**
     * Regex to match basic @Value with default value. <br>
     * <b>impl note:</b> "\p{ASCII}" refers to any <a href="https://www.w3schools.com/charsets/ref_html_ascii.asp">printable
     * ASCII character</a>, between the space (char 32 = ' ') and the and tilde (char 126 = '~').
     */
    String VALUE_BASIC_REGEX = "\\$\\{([^\\:]+)\\:{0,1}([\\p{ASCII}]*){0,1}\\}";

    Pattern BASIC_VALUE_PATTERN = Pattern.compile(VALUE_BASIC_REGEX);

    /**
     * Collect the fields annotated with [@Value] matching to provided [propertyValue].
     * @param instance The class to scan
     * @param propertyValue The property to match
     * @return the fields list
     */
    default List<Field> collectValueFields(final Object instance, final String propertyValue) {
        if (instance == null) {
            return List.of();
        }
        return findAnnotatedFields(instance.getClass(), Value.class).parallelStream()
            .filter(sutField -> sutField.getAnnotation(Value.class) != null)
            .map(sutField ->
                Pair.of(
                    sutField,
                    BASIC_VALUE_PATTERN.matcher(sutField.getAnnotation(Value.class).value())
                )
            )
            // Keep the valid fields only
            .filter(sutPair -> sutPair.getValue().matches())
            .filter(sutPair -> sutPair.getValue().group(1).equalsIgnoreCase(propertyValue))
            .map(Pair::getKey)
            .collect(Collectors.toList());
    }

}
