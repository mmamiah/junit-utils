package lu.mms.common.quality.assets.unittest;

import lu.mms.common.quality.utils.FrameworkAnnotationUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@UnitTest
class UnitAnnotationDefaultTest {

    @Test
    void shouldIncludeDefaultAnnotationSettingsWhenNoProperties() {
        // Arrange

        // Act
        final Map<Class<? extends Annotation>, Annotation> annotations =
                                                    FrameworkAnnotationUtils.getClassAnnotationMap(this.getClass());

        // Assert
        assertThat(annotations, notNullValue());
        assertThat(annotations.keySet(), hasSize(3));

        // -------- Assert UnitTest annotation
        final UnitTest unitTestAnnotation = (UnitTest) annotations.get(UnitTest.class);
        assertThat(unitTestAnnotation, notNullValue());
        assertThat(unitTestAnnotation.strictness(), equalTo(Strictness.STRICT_STUBS));
        assertThat(unitTestAnnotation.tags(), allOf(arrayWithSize(1), Matchers.hasItemInArray(UnitTest.UNIT_TEST_KEY)));

        // -------- Assert MockitoSettings annotation
        final MockitoSettings mockitoSettingsAnnotation = (MockitoSettings) annotations.get(MockitoSettings.class);
        assertThat(mockitoSettingsAnnotation, notNullValue());
        assertThat(mockitoSettingsAnnotation.strictness(), equalTo(Strictness.STRICT_STUBS));

        // -------- Assert Tag annotation
        final Tags tagsAnnotation = (Tags) annotations.get(Tags.class);
        assertThat(tagsAnnotation, notNullValue());

        final String[] tagsValue = Stream.of(tagsAnnotation.value()).map(Tag::value).toArray(String[]::new);
        assertThat(tagsValue,
                allOf(
                        arrayWithSize(1),
                        arrayContaining(UnitTest.UNIT_TEST_KEY)
                )
        );

    }

}
