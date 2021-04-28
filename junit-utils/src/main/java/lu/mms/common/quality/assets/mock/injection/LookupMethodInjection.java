package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveMocksByParameters;

/**
 * This class is in charge of the Lookup method injection, regarding the test instance target member annotated with
 * the provided annotation. <br>
 * After finding the relevant method, it's arguments are resolved from the {@link InternalMocksContext}. The selected
 * method is then called with the resolved arguments as parameters.
 */
public final class LookupMethodInjection implements Consumer<InternalMocksContext> {

    private final Class<? extends Annotation> annotationClass;

    private LookupMethodInjection(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public static LookupMethodInjection newConsumer(final Class<? extends Annotation> annotationClass) {
        return new LookupMethodInjection(annotationClass);
    }
    
    @Override
    public void accept(final InternalMocksContext mocksContext) {
        ReflectionUtils.getAllFields(
                mocksContext.getTestClass(),
                ReflectionUtils.withAnnotation(annotationClass)
        ).forEach(field -> {
            final Object target = ReflectionTestUtils.getField(mocksContext.getTestInstance(), field.getName());
            if (target == null) {
                return;
            }
            // Search for '@Autowired' methods, and fire then with the corresponding mocks as parameters.
            AnnotationSupport.findAnnotatedMethods(
                    field.getType(),
                    Autowired.class,
                    HierarchyTraversalMode.TOP_DOWN
            ).forEach(method -> {
                final Parameter[] parameters = method.getParameters();
                final Object[] args = retrieveMocksByParameters(mocksContext, parameters);

                // inject the mock in the target class, only if it is not null.
                ReflectionTestUtils.invokeMethod(target, method.getName(), args);
            });
        });

    }

}
