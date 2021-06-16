package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.MockInjectionExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.internal.configuration.InjectingAnnotationEngine;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
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
            // [1] Collect the collection of matching argument if any
            final Object[] args = ReflectionUtils.getAllConstructors(field.getType()).stream()
                    .filter(Objects::nonNull)

                    // keep the non-default constructor. We don't want to rebuild with default constructors.
                    .filter(constructor -> constructor.getParameterCount() != 0)

                    // retrieve its longest constructor parameters
                    .max(Comparator.comparing(Constructor::getParameterCount))
                    .map(Constructor::getParameters)

                    // collect the constructor parameters (as well as the array/collection mocks)
                    .map(parameters -> retrieveMocksByParameters(context, parameters))
                    .orElse(new Object[]{});

            // [2] add them to the candidate mocks collection
            Stream.of(args)
                    .filter(Objects::nonNull)
                    .forEach(mocks::add);
        });
    }

    private static boolean hasValidExtension(final Class<?> testInstanceClass) {
        // Collect from Extensions
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
        return Stream.concat(extensions, extendWithsStream)
                .anyMatch(ext -> ArrayUtils.contains(ext.value(), MockInjectionExtension.class));
    }

}
