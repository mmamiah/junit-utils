package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Spy;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveMocksByParameters;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * This class ensure the mocks from the {@link InternalMocksContext} are injected via a matching constructor to the
 * test instance member annotated with the provided annotation.
 */
public final class ConstructorInjection implements Consumer<InternalMocksContext> {

    private final Class<? extends Annotation> annotationClass;

    private ConstructorInjection(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public static ConstructorInjection newConsumer(final Class<? extends Annotation> annotationClass) {
        return new ConstructorInjection(annotationClass);
    }

    @Override
    public void accept(final InternalMocksContext mockContext) {
        ReflectionUtils.getAllFields(
                mockContext.getTestClass(),
                field -> field.isAnnotationPresent(annotationClass)
        ).forEach(field -> {
            ReflectionUtils.getAllConstructors(field.getType()).stream()
                .filter(Objects::nonNull)

                // keep the non-default constructor. We don't want to rebuild with default constructors.
                .filter(constructor -> constructor.getParameterCount() != 0)

                // Retrieve the Pair[Constructor, args]
                .map(constructor -> Pair.<Constructor<?>, Object[]>of(
                    constructor,
                    retrieveMocksByParameters(mockContext, constructor.getParameters()))
                )

                // Keep the pair for which the 'args' are non null.
                .filter(pair -> ObjectUtils.allNotNull(pair.getValue()))

                // Selecting the constructor with the most matching arguments
                .reduce((pairOne, pairTwo) -> {
                    if (pairOne.getValue().length > pairTwo.getValue().length) {
                        return pairOne;
                    }
                    return pairTwo;
                })

                // instantiate the field using the selected constructor & args
                .map(pair -> {
                    Object newInstance = BeanUtils.instantiateClass(pair.getKey(), pair.getValue());
                    // Turn the newly created field into spy if needed
                    if (isSpySourceField(mockContext.getTestInstance(), field)) {
                        newInstance = newSpy(field.getType(), newInstance, field.getName());
                    }
                    mockContext.mergeMocks(List.of(newInstance));
                    return newInstance;
                })

                // Re-inject the sut only if it has been instantiated
                .ifPresent(sut -> ReflectionTestUtils.setField(mockContext.getTestInstance(), field.getName(), sut));
        });
    }

    private static boolean isSpySourceField(final Object testInstance, final Field field) {
        return field.isAnnotationPresent(Spy.class)
        || MockUtil.isSpy(ReflectionTestUtils.getField(testInstance, field.getName()));
    }

    public static Object newSpy(final Class<?> spyClass, final Object object, final String mockName) {
        return mock(spyClass, withSettings().spiedInstance(object).name(mockName).defaultAnswer(CALLS_REAL_METHODS));
    }

}
