package lu.mms.common.quality.junit.commons;

import org.apiguardian.api.API;
import org.mockito.Mockito;
import org.reflections.ReflectionUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The '<b>MockInstanceContext</b> provide the method to retrieve the test instance mocks.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public final class TargetInstanceMocksContext {

    private final Map<String, Object> mockByNames;
    private final Map<String, Class<?>> mockByClasses;
    private final Set<Field> spyFields;

    private TargetInstanceMocksContext(final Object testInstance) {
        // get the mocks/spies from the test instance
        final List<Object> mocks = collectMocks(testInstance);
        spyFields = collectSpies(testInstance);
        mockByNames = collectMocksByName(mocks);
        mockByClasses = collectMocksByClass(mocks);
    }

    private TargetInstanceMocksContext(final List<Object> mocks) {
        spyFields = null;
        mockByNames = collectMocksByName(mocks);
        mockByClasses = collectMocksByClass(mocks);
    }

    /**
     * Create a new instance of the test instances mock context.
     * @param testInstance The test instance
     * @return The context object
     */
    public static TargetInstanceMocksContext newContext(final Object testInstance) {
        return new TargetInstanceMocksContext(testInstance);
    }

    /**
     * Create a new instance of the test instances mock context.
     * @param mocks The mocks/spies to register in the context
     * @return The context object
     */
    public static TargetInstanceMocksContext newContext(final List<Object> mocks) {
        return new TargetInstanceMocksContext(mocks);
    }

    public Object getMockByName(final String mockName) {
        return mockByNames.get(mockName);
    }

    public Set<Field> getSpyFields() {
        return spyFields;
    }

    /**
     * Get the set of mocks.
     * @return The mock collection
     */
    public Set<Object> getMocks() {
        return mockByNames.entrySet().parallelStream().map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    /**
     * Search for a mock in the context by the field name first, and if not found it will search by field type.
     * @param field The mock field to search
     * @return The mock object
     */
    public Object findMockByField(final Field field) {
        return findMockByNameOrClass(field.getName(), field.getType());
    }

    /**
     * Search for a mock in the context by the field name first, and if not found it will search by mockClass.
     * @param name The mock name
     * @param mockClass The mock class
     * @return The mock object
     */
    public Object findMockByNameOrClass(final String name, final Class<?> mockClass) {
        Object mock = mockByNames.get(name);
        if (mock == null && mockClass != Object.class) {
            mock = findMockByClass(mockClass);
        }
        return mock;
    }

    /**
     * @param mock The mock to search in the context
     * @return  true, if the context contains the given mock <br>
     *          false, otherwise
     */
    public boolean contains(final Object mock) {
        return mockByNames.containsValue(mock);
    }

    /**
     * Search for a mock in the context by the given class.
     * @param mockClass The mock class
     * @return The mock object
     */
    public Object findMockByClass(final Class<?> mockClass) {
        return mockByClasses.entrySet().stream()
            .filter(entry -> entry.getValue().equals(mockClass) || mockClass.isAssignableFrom(entry.getValue()))
            .map(entry -> {
                Object mock = mockByNames.get(entry.getKey());
                if (mock == null) {
                    mock = findAssignableMocks(mockClass).stream().findFirst().orElse(null);
                }
                return mock;
            })
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }

    /**
     * Search for all mocks in the context, that's can be assigned to the given class.
     * @param mockClass The mock class
     * @return The mocks list
     */
    public List<Object> findAssignableMocks(final Class<?> mockClass) {
        return mockByClasses.entrySet().parallelStream()
            .filter(entry -> mockClass.isAssignableFrom(entry.getValue()))
            .map(entry -> mockByNames.get(entry.getKey()))
            .collect(Collectors.toList());
    }

    private static List<Object> collectMocks(final Object testInstance) {
        return ReflectionUtils.getAllFields(testInstance.getClass()).stream()
            // map to Pair<Field, Field value>
            .map(field -> ReflectionTestUtils.getField(testInstance, field.getName()))
            // Keep the mocks only
            .filter(mock -> Mockito.mockingDetails(mock).isMock())
            .collect(Collectors.toList());
    }

    private static Map<String, Object> collectMocksByName(final List<Object> mocks) {
        return mocks.parallelStream()
            .filter(mock -> Mockito.mockingDetails(mock).isMock())
            .collect(
                Collectors.toMap(
                    mock -> Mockito.mockingDetails(mock).getMockCreationSettings().getMockName().toString(),
                    Function.identity()
                )
            );
    }

    private static Set<Field> collectSpies(final Object testInstance) {
        return ReflectionUtils.getAllFields(
            testInstance.getClass(),
            field -> {
                final Object fieldValue = ReflectionTestUtils.getField(testInstance, field.getName());
                return Mockito.mockingDetails(fieldValue).isSpy();
            }
        );
    }

    private static Map<String, Class<?>> collectMocksByClass(final List<Object> mocks) {
        return mocks.parallelStream()
            .collect(
                Collectors.toMap(
                    mock -> Mockito.mockingDetails(mock).getMockCreationSettings().getMockName().toString(),
                    Object::getClass
                )
            );
    }
}
