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
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;


// These annotations are tested in the test assert
@UnitTest(tags = "Hello")
@Tag("world")
@Tag("tac")
class UnitAnnotationTagsTest {

    @Test
    void shouldIncludeMockitoSettingsAndAdditionalTagsWhenTagsSpecified() {
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
        assertThat(unitTestAnnotation.tags(), allOf(
            arrayWithSize(1),
            not(Matchers.hasItemInArray(UnitTest.UNIT_TEST_KEY)))
        );

        // -------- Assert MockitoSettings annotation
        final MockitoSettings mockitoSettingsAnnotation = (MockitoSettings) annotations.get(MockitoSettings.class);
        assertThat(mockitoSettingsAnnotation, notNullValue());
        assertThat(mockitoSettingsAnnotation.strictness(), equalTo(Strictness.STRICT_STUBS));

        // -------- Assert Tag annotation
        final Tag tagAnnotation = (Tag) annotations.get(Tag.class);
        assertThat(tagAnnotation, nullValue());

        // -------- Assert Tag annotation
        final Tags tagsAnnotation = (Tags) annotations.get(Tags.class);
        assertThat(tagsAnnotation, notNullValue());

        final String[] tagsValue = Stream.of(tagsAnnotation.value()).map(Tag::value).toArray(String[]::new);
        assertThat(tagsValue,
                allOf(
                    arrayWithSize(3),
                    arrayContainingInAnyOrder("Hello", "world", "tac")
                )
        );

    }

}
