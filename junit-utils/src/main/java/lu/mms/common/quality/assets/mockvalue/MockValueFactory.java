package lu.mms.common.quality.assets.mockvalue;

import lu.mms.common.quality.assets.AssetFactory;
import lu.mms.common.quality.assets.unittest.UnitTest;
import lu.mms.common.quality.utils.ConfigurationPropertiesUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static lu.mms.common.quality.utils.FrameworkAnnotationUtils.buildReflections;

/**
 * The MockValueFactory ensure that for any declared {@link MockValue}, the related class is extended with the correct
 * extension ({@link MockValueExtension}).
 */
public class MockValueFactory implements AssetFactory<MockValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockValueFactory.class);
    private static final String ERROR_TEMPLATE = "@MockValue on the field '{}' but "
                                                    + "the class '{}' is not extended with MockValueExtension.";

    public Class<MockValue> getType() {
        return MockValue.class;
    }

    @Override
    public void apply() {
        final String packageToScan = ConfigurationPropertiesUtils.getPackageToScan();
        final Reflections reflections = buildReflections(packageToScan);
        reflections.getFieldsAnnotatedWith(getType())
                    .forEach(this::extensionValidation);
    }

    private void extensionValidation(final Field field) {
        final Class<?> declaringClass = field.getDeclaringClass();
        if (declaringClass.isAnnotationPresent(UnitTest.class)) {
            return;
        }
        final Extensions extensions = declaringClass.getDeclaredAnnotation(Extensions.class);
        final ExtendWith[] extendWiths = declaringClass.getDeclaredAnnotationsByType(ExtendWith.class);
        if (!(containsExtension(extendWiths) || (extensions != null && containsExtension(extensions.value())))) {
            LOGGER.warn(ERROR_TEMPLATE, field.getName(), declaringClass.getSimpleName());
        }
    }

    private boolean containsExtension(final ExtendWith... extendWith) {
        boolean extensionExists = false;
        if (extendWith != null && extendWith.length > 0) {
            extensionExists = Stream.of(extendWith)
                .flatMap(extWith -> Stream.of(extWith.value()))
                .anyMatch(extension -> extension == MockValueExtension.class);
        }
        return extensionExists;
    }

}
