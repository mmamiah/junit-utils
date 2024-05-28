package lu.mms.common.quality.assets;

import lu.mms.common.quality.platform.SpiConfiguration;

import java.lang.annotation.Annotation;

/**
 * The Asset Factory handle the way an asset should be applied to a class. <br>
 * By "apply", we understand adding a JUnit 5 extension for example, when applicable.
 * @param <T> an annotation type
 */
public interface AssetFactory<T extends Annotation> {

    /**
     * Ordering factories, as to be applied in {@link SpiConfiguration}. <br>
     * The lowest "order" value have the highest precedence. That means,
     * the factory with "order=0" will be applied first, then one with "order=1", and so on.
     * When not implemented, it will be applied at the end (or at the lowest precedence/priority). <br>
     * @return The factory order
     */
    default Integer getOrder() {
        return null;
    }

    Class<T> getType();

    /**
     * Search in the class path for class with annotation and <br>
     * apply its configuration to target classes.
     */
    void apply();

}
