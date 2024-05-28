package lu.mms.common.quality.junit.assets.bdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodDescriptor;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.Function;

/**
 * The Method Name Comparator utils.
 */
public final class MethodNameComparatorUtils {

    static final String GIVEN = "given";
    static final String WHEN = "when";
    static final String THEN = "then";

    private MethodNameComparatorUtils() {
        // hidden constructor
    }

    /**
     * The test method descriptor comparator.
     * @return The Comparator object
     */
    public static Comparator<MethodDescriptor> getMethodDescriptorComparator() {
        return Comparator.comparing(MethodDescriptor::getDisplayName, new DisplayNameComparator(GIVEN))
                .thenComparing(MethodDescriptor::getDisplayName, new DisplayNameComparator(WHEN))
                .thenComparing(MethodDescriptor::getDisplayName, new DisplayNameComparator(THEN));
    }

    /**
     * The test method DisplayName comparator.
     * @return The Comparator object
     */
    public static Comparator<Method> getDisplayNameComparator() {
        return Comparator.comparing(getMethodDisplayName(), new DisplayNameComparator(GIVEN))
                .thenComparing(getMethodDisplayName(), new DisplayNameComparator(WHEN))
                .thenComparing(getMethodDisplayName(), new DisplayNameComparator(THEN));
    }

    private static Function<Method, String> getMethodDisplayName() {
        return method -> {
            final DisplayName displayName = method.getAnnotation(DisplayName.class);
            String methodName = method.getName();
            if (displayName != null) {
                methodName = displayName.value();
            }
            return methodName;
        };
    }

}
