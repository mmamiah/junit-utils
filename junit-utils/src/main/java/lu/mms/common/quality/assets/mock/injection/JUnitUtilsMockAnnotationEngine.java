package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.MockInjectionExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.internal.configuration.InjectingAnnotationEngine;
import org.reflections.ReflectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveMocksByParameters;

/**
 * This class is a custom {@link InjectingAnnotationEngine} Mockito plugin.
 * The goal of this class is to scan the subject under test constructor, and provide the relevant Array/Collection of
 * mocks to easy the (SUT) constructor injection.
 */
public class JUnitUtilsMockAnnotationEngine extends InjectingAnnotationEngine {

    @Override
    protected void onInjection(final Object testClassInstance, final Class<?> clazz,
                               final Set<Field> mockDependentFields,
                               final Set<Object> mocks) {
        // Validation: If the test class is not extended by the 'MockInjectionExtension' then exit
        if (!hasValidExtension(clazz)) {
            return;
        }

        final InternalMocksContext context = InternalMocksContext.newContext(null, clazz, testClassInstance, null);
        mockDependentFields.forEach(field -> {
            // retrieve the non-default constructor
            final Constructor<?> constructor = ReflectionUtils.getAllConstructors(field.getType()).stream()
                    .filter(Objects::nonNull)

                    // keep the non-default constructor. We don't want to rebuild with default constructors.
                    .filter(constructorItem -> constructorItem.getParameterCount() != 0)

                    // retrieve its longest constructor parameters
                    .max(Comparator.comparing(Constructor::getParameterCount))
                    .orElse(null);

            if (constructor == null) {
                return;
            }

            // Instantiate the field
            final Object[] args = retrieveMocksByParameters(context, constructor.getParameters());
            final Object instance = BeanUtils.instantiateClass(constructor, args);
            ReflectionTestUtils.setField(testClassInstance, field.getName(), instance);

            // remove the mock dependent fields to avoid any other instantiation
            if (clazz.getSuperclass() == Object.class) {
                mockDependentFields.remove(field);
            }
        });
    }

    private static boolean hasValidExtension(final Class<?> testInstanceClass) {
        // Collect from Extensions
        Stream<ExtendWith> extensionsExtendWithTestUtils = Stream.empty();
        if (testInstanceClass.isAnnotationPresent(ExtendWithTestUtils.class)) {
            extensionsExtendWithTestUtils = Stream.of(testInstanceClass.getAnnotationsByType(ExtendWithTestUtils.class))
                    .flatMap(extension -> Stream.of(extension.annotationType().getAnnotationsByType(ExtendWith.class)));
        }

        Stream<ExtendWith> extensions = Stream.empty();
        if (testInstanceClass.isAnnotationPresent(Extensions.class)) {
            extensions = Stream.of(testInstanceClass.getAnnotationsByType(Extensions.class))
                    .flatMap(extension -> Stream.of(extension.value()));
        }

        // Collect the ExtendWith
        Stream<ExtendWith> extendWithsStream = Stream.empty();
        if (testInstanceClass.isAnnotationPresent(ExtendWith.class)) {
            extendWithsStream = Stream.of(testInstanceClass.getAnnotationsByType(ExtendWith.class));

        }

        // check if any of them has the required extension.
        return Stream.of(extensions, extendWithsStream, extensionsExtendWithTestUtils)
                .flatMap(Function.identity())
                .anyMatch(ext -> ArrayUtils.contains(ext.value(), MockInjectionExtension.class));
    }

}
