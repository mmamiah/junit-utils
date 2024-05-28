package lu.mms.common.quality.platform;

import lu.mms.common.quality.assets.AssetFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import static lu.mms.common.quality.platform.SpiConfiguration.ROOT_PACKAGE;
import static lu.mms.common.quality.platform.SpiConfiguration.retrieveFactories;
import static lu.mms.common.quality.utils.FrameworkUtils.findCandidateComponents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

@Disabled
class SpiConfigurationTest {

    @Test
    void shouldConfirmEachFactoryHasBeenProperlyRegistered() {
        // Arrange
        final Set<Class<? extends Annotation>> frameworkAnnotationClasses = findCandidateComponents(new AnnotationTypeFilter(Annotation.class), ROOT_PACKAGE);

        // Act
        final Map<Class<Annotation>, AssetFactory<Annotation>> factories = retrieveFactories();

        // Assert
        assertThat(factories.size(), equalTo(1));
        assertThat(factories.size(), lessThanOrEqualTo(frameworkAnnotationClasses.size()));
        factories.entrySet().stream()
            // Keeping only defined annotations
            .filter(entry -> entry.getKey().getPackage().getName().contains(ROOT_PACKAGE))
            .forEach(entry -> {
                assertThat(entry.getKey(), equalTo(entry.getValue().getType()));
                assertThat(frameworkAnnotationClasses, hasItem(entry.getKey()));
                assertThat(entry.getKey(), notNullValue());
            });

    }

}
