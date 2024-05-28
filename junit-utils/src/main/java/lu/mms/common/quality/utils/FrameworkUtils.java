package lu.mms.common.quality.utils;


import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lu.mms.common.quality.platform.SpiConfiguration.ROOT_PACKAGE;

/**
 * Utility class for 'junit-utils' framework.
 */
public final class FrameworkUtils {

    private FrameworkUtils() {
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
                Scanners.FieldsAnnotated ,
                Scanners.MethodsAnnotated,
                Scanners.ConstructorsAnnotated,
                Scanners.SubTypes,
                Scanners.TypesAnnotated,
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
        return Stream.of(klass.getDeclaredAnnotations())
                .map(annotation -> Map.entry(annotation.annotationType(), annotation))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Add annotation to a test class.
     * @param targetClass The target class
     * @param annotation The annotation to add
     * @param <A> The annotation type
     */
    public static <A extends Annotation> void addAnnotationToClass(final Class<?> targetClass, final A annotation) {
        final Map<Class<? extends Annotation>, Annotation> annotations = FrameworkUtils
            .getClassAnnotationMap(targetClass);
        if (annotations == null) {
            return;
        }
        annotations.put(annotation.annotationType(), annotation);
    }

    public static <T> Set<Class<? extends T>> findAssignableCandidate(final Class<T> klass) {
        return findCandidateComponents(new AssignableTypeFilter(klass), ROOT_PACKAGE);
    }

    public static <T> Set<Class<? extends T>> findCandidateComponents(final TypeFilter typeFilter, final String packageName) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(typeFilter);

        Set<BeanDefinition> components = provider.findCandidateComponents(packageName);
        final Set<Class<? extends T>> factoryClasses = new HashSet<>();
        for (BeanDefinition component : components) {
            try {
                Class<?> cls = Class.forName(component.getBeanClassName());
                factoryClasses.add((Class<? extends T>) cls);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return factoryClasses;
    }

}
