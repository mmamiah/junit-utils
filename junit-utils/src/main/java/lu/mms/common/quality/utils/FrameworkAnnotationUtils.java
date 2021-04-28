package lu.mms.common.quality.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.platform.commons.util.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility class for 'junit-utils' framework.
 */
public final class FrameworkAnnotationUtils {

    private FrameworkAnnotationUtils() {
        // Empty constructor
    }

    /**
     * Build a Reflections object with all the scanners: <br>
     *     <ul>
     *         <li>FieldAnnotationsScanner</li>
     *         <li>MemberUsageScanner</li>
     *         <li>MethodAnnotationsScanner</li>
     *         <li>SubTypesScanner</li>
     *         <li>TypeAnnotationsScanner</li>
     *         <li>TypeElementsScanner</li>
     *     </ul>
     * @param packageName The package to scan
     * @return The Reflections object
     */
    public static Reflections buildReflections(final String... packageName) {
        final ConfigurationBuilder configBuilder = new ConfigurationBuilder()
            .setScanners(
                new FieldAnnotationsScanner(),
                new MethodAnnotationsScanner(),
                new SubTypesScanner(false),
                new TypeAnnotationsScanner(),
                new TypeElementsScanner()
            );
        if (packageName != null) {
            final URL[] urls = Stream.of(packageName)
                .map(url -> StringUtils.defaultIfBlank(url, StringUtils.EMPTY))
                .distinct()
                .map(ClasspathHelper::forPackage)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
            configBuilder.setUrls(urls);
        }

        return new Reflections(configBuilder);
    }

    public static Map<Class<? extends Annotation>, Annotation> getClassAnnotationMap(final Class<?> klass) {
        return ReflectionUtils.findMethod(Class.class, "annotationData")
            .map(method -> ReflectionUtils.invokeMethod(method, klass))
            .map(annotationData -> {
                final Field field = Stream.of(annotationData.getClass().getDeclaredFields())
                    .filter(tempField -> tempField.getName().equals("declaredAnnotations"))
                    .findFirst()
                    .orElse(null);
                return Pair.of(annotationData, field);
            })
            .filter(pair -> pair.getValue() != null)
            .map(pair -> (Map<Class<? extends Annotation>, Annotation>) ReflectionTestUtils
                        .getField(pair.getKey(), pair.getValue().getName())
            )
            .orElse(Map.of());
    }

    /**
     * Add annotation to a test class.
     * @param targetClass The target class
     * @param annotation The annotation to add
     * @param <A> The annotation type
     */
    public static <A extends Annotation> void addAnnotationToClass(final Class<?> targetClass, final A annotation) {
        final Map<Class<? extends Annotation>, Annotation> annotations = FrameworkAnnotationUtils
            .getClassAnnotationMap(targetClass);
        if (annotations == null) {
            return;
        }
        annotations.put(annotation.annotationType(), annotation);
    }

}
