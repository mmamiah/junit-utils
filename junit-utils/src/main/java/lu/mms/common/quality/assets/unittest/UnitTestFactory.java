package lu.mms.common.quality.assets.unittest;

import lu.mms.common.quality.assets.AssetFactory;
import lu.mms.common.quality.junit.platform.MockitoFactory;
import lu.mms.common.quality.junit.platform.TagFactory;
import lu.mms.common.quality.utils.ConfigurationPropertiesUtils;
import org.junit.platform.commons.util.AnnotationUtils;
import org.reflections.Reflections;

import java.util.Optional;

import static lu.mms.common.quality.utils.FrameworkAnnotationUtils.buildReflections;

/**
 * The {@link UnitTest} annotation factory.
 */
public class UnitTestFactory implements AssetFactory<UnitTest> {

    @Override
    public Integer getOrder() {
        /*
        * Value '0' as This factory should be applied as the first one.
        * It is initializing the structural settings of this framework.
        * */
        return 0;
    }

    @Override
    public Class<UnitTest> getType() {
        return UnitTest.class;
    }

    @Override
    public void apply() {
        final String packageToScan = ConfigurationPropertiesUtils.getPackageToScan();
        final Reflections reflections = buildReflections(packageToScan);
        reflections.getTypesAnnotatedWith(getType())
            .forEach(type -> {
                final Optional<UnitTest> optUnit = AnnotationUtils.findAnnotation(type, getType());
                // Apply the config for each annotation (per class)
                optUnit.ifPresent(unitTest -> {
                    TagFactory.addTags(type, unitTest.tags());
                    MockitoFactory.addMockitoSettings(type, unitTest.strictness());
                });
            });
    }

}
