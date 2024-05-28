package lu.mms.common.quality.junit.assets.mock.injection;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.reinforcement.MockReinforcementHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveFirstGenericType;
import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveMocksCollectionWithSafeType;

/**
 * This class ensure the mocks from the {@link InternalMocksContext} are injected via field/setter when relevant, to the
 * test instance target member annotated with the provided annotation.
 */
public final class FieldAndSetterInjection implements Consumer<InternalMocksContext>, MockReinforcementHandler {

    private final Class<? extends Annotation> annotationClass;

    private FieldAndSetterInjection(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Instantiate a new mock injection consumer.
     * @param annotationClass   The annotation to search.
     * @return  the mock injection consumer
     */
    public static FieldAndSetterInjection newConsumer(final Class<? extends Annotation> annotationClass) {
        return new FieldAndSetterInjection(annotationClass);
    }

    @Override
    public void accept(final InternalMocksContext mocksContext) {
        final Set<Field> mockDependentFields = ReflectionUtils.getAllFields(
                mocksContext.getTestClass(),
                ReflectionUtils.withAnnotation(annotationClass)
        );

        // Try field injection & setter injection
        injectMocksOnFields(mockDependentFields, mocksContext.getMocks(), mocksContext.getTestInstance());

        // Try collection injection
        injectMockCollections(mocksContext, mockDependentFields);
    }

    @Override
    public void injectMocksOnFields(final Set<Field> needingInjection, final Set<Object> mocks,
                                    final Object ofInstance) {
        // ensure there is mocks type duplication, and if so, the spies will have priority
        if (ObjectUtils.anyNull(needingInjection, mocks, ofInstance)) {
            return;
        }

        needingInjection.forEach(memberField -> {
            // retrieve the member needing injection
            final Object member = ReflectionTestUtils.getField(ofInstance, memberField.getName());
            if (member == null) {
                return;
            }

            // get the member fields to match the mocks and inject then if relevant
            ReflectionUtils.getAllFields(member.getClass()).forEach(field -> {
                mocks.stream()
                    // Keep the assignable type only
                    .filter(mock -> field.getType().isAssignableFrom(MockUtil.getMockSettings(mock).getTypeToMock()))
                    // reduce the selection, prefering any mock which name matches with the field name, or the
                    // spies over the mocks.
                    .reduce((oldMock, newMock) -> {
                        Object selection = oldMock;
                        // prefer a Spy or a mock which name matches with the field name.
                        final String newMockName = MockUtil.getMockName(newMock).toString();
                        if (MockUtil.isSpy(newMock) || field.getName().equals(newMockName)) {
                            selection = newMock;
                        }
                        return selection;
                    })
                    // Inject the selected mock into the target member.
                    .ifPresent(mock -> ReflectionTestUtils.setField(member, field.getName(), mock));
            });

        });

    }

    /**
     * Search for collection of objects applicable to the mock declared by the user, and initialize the with
     * corresponding mocks.
     * @param mockContext   The mock context
     * @param fields    The test instance target fields (i.e @Spy, @InjectMock)
     */
    private void injectMockCollections(final InternalMocksContext mockContext, final Set<Field> fields) {

        fields.stream()
            // collect the Pair[source field (in test instance), target field]
            .flatMap(source -> {
                final Set<Field> targetFields = ReflectionUtils.getAllFields(
                        source.getType(),
                        ReflectionUtils.withTypeAssignableTo(Collection.class).or(field -> field.getType().isArray())
                );
                return targetFields.stream().map(target -> Pair.of(source, target));
            })
            .forEach(pair -> {
                final Field target = pair.getValue();
                // retrieve the generic type
                final Class<?> componentType = retrieveFirstGenericType(target.getType(), target.getGenericType());
                final int modifiers = target.getModifiers();
                if (componentType == null || Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
                    return;
                }

                // collect all the assignable mocks
                final List<?> mocks = mockContext.findAssignableMocks(componentType);
                final Object mocksCollection = retrieveMocksCollectionWithSafeType(target.getType(), mocks);
                final Object source = ReflectionTestUtils.getField(
                        mockContext.getTestInstance(), pair.getKey().getName()
                );
                if (source == null) {
                    return;
                }
                ReflectionTestUtils.setField(source, target.getName(), mocksCollection);
            });

    }
}
