package lu.mms.common.quality.junit.platform;

import lu.mms.common.quality.utils.FrameworkAnnotationUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.platform.commons.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class for {@link Tag} and {@link Tags} handling.
 */
public final class TagFactory {

    private TagFactory() {
        // hidden constructor
    }

    /**
     * Merge all {@link Tag} annotation into a {@link Tags} annotation and add it to the target class.
     * @param targetClass The target class
     * @param tag The tags to add
     */
    public static void addTags(final Class<?> targetClass, final String... tag) {
        if (tag == null) {
            return;
        }
        // Add the new annotation to the test class
        final Map<Class<? extends Annotation>, Annotation> annotations = FrameworkAnnotationUtils
                                                                            .getClassAnnotationMap(targetClass);
        assert annotations != null;
        final Tags removedTags = (Tags) annotations.remove(Tags.class);
        final Tag removedTag = (Tag) annotations.remove(Tag.class);
        final List<Tag> actualTags = appendTags(removedTag, removedTags);

        // Merging the user tags with the default one.
        final Set<String> strTags = new HashSet<>(Arrays.asList(tag));

        final Stream<Tag> newTagsStream = strTags.stream()
            .filter(StringUtils::isNotBlank)
            .map(TagFactory::newTag);

        final Tag[] newTags = Stream.concat(newTagsStream, actualTags.stream()).toArray(Tag[]::new);

        annotations.put(Tags.class, TagFactory.mergeToTags(newTags));
    }

    /**
     * Build a {@link Tag} annotation.
     * @param tag The tag to create
     * @return The created @Tag object.
     */
    private static Tag newTag(final String tag) {
        return new Tag() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Tag.class;
            }

            @Override
            public String value() {
                return tag;
            }
        };
    }

    /**
     * Build {@link Tags} annotations.
     * @param tag The {@link Tag} to merge into a {@link Tags} annotation.
     * @return The @Tags annotation object.
     */
    private static Tags mergeToTags(final Tag... tag) {
        return new Tags() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Tags.class;
            }

            @Override
            public Tag[] value() {
                if (tag == null) {
                    return new Tag[0];
                }
                return tag;
            }
        };
    }

    private static List<Tag> appendTags(final Tag tag, final Tags tags) {
        List<Tag> newTags = new ArrayList<>();
        if (tags != null) {
            newTags = Arrays.asList(tags.value());
        }
        if (tag != null) {
            newTags.add(tag);
        }
        return newTags;
    }

}
