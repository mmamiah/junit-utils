package lu.mms.common.quality.junit.platform;

import lu.mms.common.quality.utils.FrameworkAnnotationUtils;
import org.junit.platform.commons.util.AnnotationUtils;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * The mockito factory. <br>
 * This class mainly centralize all Mockito operations.
 */
public final class MockitoFactory {

    private static final String CONFLICT_FORMAT = "Ambiguous setting found: [%s] is different from  [%s].";

    private MockitoFactory() {
        // hidden constructor
    }

    /**
     * Add the {@link MockitoSettings} to the target class.
     * @param targetClass The target class
     * @param strictness The Mockito Strictness
     */
    public static void addMockitoSettings(final Class<?> targetClass, final Strictness strictness) {
        // Ensure that we do not have conflict with other [@MockitoSettings] if existing
        final Optional<MockitoSettings> settings = AnnotationUtils.findAnnotation(targetClass, MockitoSettings.class);
        final boolean isConflicting = settings.isPresent() && settings.get().strictness() != strictness;
        assert !isConflicting : String.format(CONFLICT_FORMAT, "@MockitoSettings.strictness", "@UnitTest.strictness");
        // retrieve the [@UnitTest.strictness]
        final MockitoSettings newSettings = newMockitoSettings(strictness);

        // Add the new annotation to the test class
        FrameworkAnnotationUtils.addAnnotationToClass(targetClass, newSettings);
    }

    /**
     * build a {@link MockitoSettings} annotation, with the specified {@link Strictness}.
     * @param strictness The Mockito Strictness
     * @return The Mockito Settings annotation object.
     */
    private static MockitoSettings newMockitoSettings(final Strictness strictness) {
        return new MockitoSettings() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MockitoSettings.class;
            }

            @Override
            public Strictness strictness() {
                return strictness;
            }
        };
    }

}
