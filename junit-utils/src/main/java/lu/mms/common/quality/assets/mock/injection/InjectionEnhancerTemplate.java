package lu.mms.common.quality.assets.mock.injection;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

/**
 * This class the three (3) injection mode are applied to the selection element as follow:
 * <ol>
 *     <li>Apply the constructor injection</li>
 *     <li>Apply the field/setter injection</li>
 *     <li>Apply the Lookup method injection</li>
 * </ol>
 *
 * The arguments to be injected are resolved from the {@link InternalMocksContext} (mock/spies declared in the
 * test instance).
 */
public final class InjectionEnhancerTemplate implements Consumer<InternalMocksContext> {

    private final Class<? extends Annotation> annotationClass;

    private InjectionEnhancerTemplate(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public static InjectionEnhancerTemplate newTemplate(final Class<? extends Annotation> annotationClass) {
        return new InjectionEnhancerTemplate(annotationClass);
    }

    @Override
    public void accept(final InternalMocksContext mocksContext) {

        // constructor injection
        ConstructorInjection.newConsumer(annotationClass)

                // field & setter injection
                .andThen(FieldAndSetterInjection.newConsumer(annotationClass))

                // Lookup method injection
                .andThen(LookupMethodInjection.newConsumer(annotationClass))

                .accept(mocksContext);

    }

}
