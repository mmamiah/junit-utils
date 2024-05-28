package lu.mms.common.quality.commons;

import org.mockito.internal.util.MockUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * The Predicate to check if a field is a Mock or a Spy.
 */
public final class MockSpyFieldPredicate implements Predicate<Field> {

    private final Object instance;

    private MockSpyFieldPredicate(final Object instance) {
        this.instance = instance;
    }

    /**
     * Create New Predicate instance.
     * @param instance The source object containing the fields
     * @return The MockSpyPredicate
     */
    public static MockSpyFieldPredicate newPredicate(final Object instance) {
        return new MockSpyFieldPredicate(instance);
    }

    @Override
    public boolean test(final Field field) {
        final Object fieldValue = ReflectionTestUtils.getField(instance, field.getName());
        return MockUtil.isMock(fieldValue);
    }
}
