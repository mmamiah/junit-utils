package lu.mms.common.quality.junit.assets.mock.injection;

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
    private Consumer<InternalMocksContext> injectionConsumer = null;

    private InjectionEnhancerTemplate(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    private InjectionEnhancerTemplate(final Class<? extends Annotation> annotationClass,
                                      final Consumer<InternalMocksContext> injectionConsumer) {
        this.annotationClass = annotationClass;
        this.injectionConsumer = injectionConsumer;
    }

    /**
     * Create a new Injection Template. <br>
     * This template will inject the mocks as follow:
     * <ol>
     *     <li>Apply the constructor injection</li>
     *     <li>Apply the field/setter injection</li>
     *     <li>Apply the Lookup method injection</li>
     * </ol>
     * @param annotationClass The target object annotation
     * @return  The new template object
     */
    public static InjectionEnhancerTemplate newTemplate(final Class<? extends Annotation> annotationClass) {
        return new InjectionEnhancerTemplate(annotationClass);
    }

    /**
     * Create a new Constructor Injection Template. <br>
     * This template will inject the mocks as follow:
     * <ol>
     *     <li>Apply the constructor injection</li>
     *     <li>Apply the field/setter injection</li>
     *     <li>Apply the Lookup method injection</li>
     * </ol>
     * @param annotationClass The target object annotation
     * @return  The new template object
     */
    public static InjectionEnhancerTemplate newConstructorInjectionTemplate(final Class<? extends Annotation> annotationClass) {
        return new InjectionEnhancerTemplate(
                annotationClass,
                ConstructorInjection.newConsumer(annotationClass)
        );
    }

    /**
     * Create a new Field Injection Template. <br>
     * This template will inject the mocks as follow:
     * <ol>
     *     <li>Apply the field/setter injection</li>
     *     <li>Apply the Lookup method injection</li>
     * </ol>
     * @param annotationClass The target object annotation
     * @return  The new template object
     */
    public static InjectionEnhancerTemplate newFieldInjectionTemplate(final Class<? extends Annotation> annotationClass) {
        return new InjectionEnhancerTemplate(
                annotationClass,
                FieldAndSetterInjection.newConsumer(annotationClass)
        );
    }

    @Override
    public void accept(final InternalMocksContext mocksContext) {
        Consumer<InternalMocksContext> localConsumer = injectionConsumer;
        if (localConsumer == null) {
            // constructor injection
            localConsumer = ConstructorInjection.newConsumer(annotationClass);
        }

        if (localConsumer instanceof ConstructorInjection) {
            // field & setter injection
            localConsumer = localConsumer.andThen(FieldAndSetterInjection.newConsumer(annotationClass));
        }

        if (!(localConsumer instanceof LookupMethodInjection)) {
            // Lookup method injection
            localConsumer = localConsumer.andThen(LookupMethodInjection.newConsumer(annotationClass));
        }

        localConsumer.accept(mocksContext);

    }

}
