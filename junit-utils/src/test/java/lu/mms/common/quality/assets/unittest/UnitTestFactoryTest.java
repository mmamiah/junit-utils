package lu.mms.common.quality.assets.unittest;

import lu.mms.common.quality.utils.FrameworkAnnotationUtils;
import org.hamcrest.core.AnyOf;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.IsEqual.equalTo;

class UnitTestFactoryTest {

    private static final String TAG_ONE = "tag_one";
    private static final String TAG_TWO = "tag_two";

    private UnitTestFactory sut = new UnitTestFactory();

    @Test
    void shouldConfirmFactoryType() {
        // Assert
        assertThat(sut.getType(), equalTo(UnitTest.class));
    }

    @Test
    void shouldConfirmFactoryOrder() {
        // Assert
        assertThat(sut.getOrder(), equalTo(0));
    }

    @Test
    void shouldApplyDefaultConfigWhenSimpleAnnotationApplied() {
        // Arrange
        final SimpleAnnotatedTestcase item = new SimpleAnnotatedTestcase();

        // Assert
        final Map<Class<? extends Annotation>, Annotation> annotations = FrameworkAnnotationUtils
                                                                            .getClassAnnotationMap(item.getClass());
        assertThat(annotations.containsKey(Tag.class), equalTo(false));
        assertThat(annotations.containsKey(Tags.class), equalTo(true));

        // Tags
        final Tags tags = (Tags) annotations.get(Tags.class);
        assertThat(tags.value(), arrayWithSize(1));
        assertThat(tags.value()[0].value(), equalTo("unit_test"));

        // strictness
        final MockitoSettings mockitoSettings = (MockitoSettings) annotations.get(MockitoSettings.class);
        assertThat(mockitoSettings.strictness(), equalTo(Strictness.STRICT_STUBS));

        // UnitTest
        final UnitTest unitTest = (UnitTest) annotations.get(UnitTest.class);
        assertThat(unitTest.returnMocks(), equalTo(true));
        assertThat(unitTest.initMocks(), equalTo(true));
    }

    @Test
    void shouldApplyCustomTagWhenTagProvided() {
        // Arrange
        final AnnotatedTestcaseWithTag item = new AnnotatedTestcaseWithTag();

        // Assert
        final Map<Class<? extends Annotation>, Annotation> annotations = FrameworkAnnotationUtils
            .getClassAnnotationMap(item.getClass());
        assertThat(annotations.containsKey(Tag.class), equalTo(false));
        assertThat(annotations.containsKey(Tags.class), equalTo(true));

        // Tags
        final Tags tags = (Tags) annotations.get(Tags.class);
        assertThat(tags.value(), arrayWithSize(2));
        Stream.of(tags.value()).forEach(tag -> {
            assertThat(tag.value(), AnyOf.anyOf(equalTo(TAG_ONE), equalTo(TAG_TWO)));
        });
    }

    @Test
    void shouldApplyStrictnessWhenDefinedInUnitTestAnnotation() {
        // Arrange
        final AnnotatedTestcaseWithStrictness item = new AnnotatedTestcaseWithStrictness();

        // Assert
        final Map<Class<? extends Annotation>, Annotation> annotations = FrameworkAnnotationUtils
                                                                            .getClassAnnotationMap(item.getClass());
        assertThat(annotations.containsKey(MockitoSettings.class), equalTo(true));

        // strictness
        final MockitoSettings mockitoSettings = (MockitoSettings) annotations.get(MockitoSettings.class);
        assertThat(mockitoSettings.strictness(), equalTo(Strictness.LENIENT));

    }

    @UnitTest
    private static class SimpleAnnotatedTestcase {
        // empty class
    }

    @UnitTest(tags = {TAG_ONE, TAG_TWO})
    private static class AnnotatedTestcaseWithTag {
        // empty class
    }

    @UnitTest(strictness = Strictness.LENIENT)
    private static class AnnotatedTestcaseWithStrictness {
        // empty class
    }

}
