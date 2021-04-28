package lu.mms.common.quality.junit.platform;

import lu.mms.common.quality.assets.AssetFactory;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import static lu.mms.common.quality.junit.platform.SpiConfiguration.ROOT_PACKAGE;
import static lu.mms.common.quality.junit.platform.SpiConfiguration.retrieveFactories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

class SpiConfigurationTest {

    @Test
    void shouldConfirmEachFactoryHasBeenProperlyRegistered() {
        // Arrange
        final Reflections reflections = new Reflections(ROOT_PACKAGE);
        final Set<Class<? extends AssetFactory>> factoryClasses = reflections.getSubTypesOf(AssetFactory.class);
        final Set<Class<? extends Annotation>> frameworkAnnotationClasses = reflections.getSubTypesOf(Annotation.class);

        // Act
        final Map<Class<Annotation>, AssetFactory<Annotation>> factories = retrieveFactories();

        // Assert
        assertThat(factories.size(), equalTo(factoryClasses.size()));
        assertThat(factories.size(), lessThanOrEqualTo(frameworkAnnotationClasses.size()));
        factories.entrySet().stream()
            // Keeping only defined annotations
            .filter(entry -> entry.getKey().getPackage().getName().contains(ROOT_PACKAGE))
            .forEach(entry -> {
                assertThat(entry.getKey(), equalTo(entry.getValue().getType()));
                assertThat(frameworkAnnotationClasses, hasItem(entry.getKey()));
                assertThat(entry.getKey(), notNullValue());
                assertThat(factoryClasses, hasItem(entry.getValue().getClass()));
            });

    }

}
