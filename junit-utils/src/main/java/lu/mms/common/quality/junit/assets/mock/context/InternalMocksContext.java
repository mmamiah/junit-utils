package lu.mms.common.quality.junit.assets.mock.context;

import lu.mms.common.quality.junit.assets.AssetVisitor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The '<b>MockInstanceContext</b> collect the test instance mocks and spies, and provide the method to retrieve
 * and access them smoothly.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public final class InternalMocksContext implements AssetVisitor<Consumer<InternalMocksContext>> {
    private static final String MOCKS_CONTEXT_ENABLED_TEMPLATE = "Mocks context has been instantiated for '{}().{}.";

    private final Object testInstance;
    private final Class<?> testClass;
    private final String testMethodName;
    private final Map<String, Object> mockByNames = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> mockByClasses = new ConcurrentHashMap<>();
    private final Set<Field> spyFields = new HashSet<>();

    private InternalMocksContext() {
        this.testInstance = null;
        this.testClass = null;
        this.testMethodName = null;
    }

    private InternalMocksContext(final Logger logger, final Class<?> testClass, final Object testInstance,
                                 final String testMethodName) {

        this.testInstance = testInstance;
        this.testClass = testClass;
        this.testMethodName = testMethodName;

        refresh();

        logContextState(logger, testClass, testMethodName);

    }

    /**
     * Create a new empty {@link InternalMocksContext}.
     *
     * @return The empty context object
     */
    public static InternalMocksContext newEmptyContext() {
        return new InternalMocksContext();
    }

    /**
     * Create a new {@link InternalMocksContext}.
     * @param logger The parent class logger
     * @param testClass The test class
     * @param testInstance The test instance
     * @param testMethodName The test method name
     *
     * @return The context object
     */
    public static InternalMocksContext newContext(final Logger logger, final Class<?> testClass,
                                                  final Object testInstance, final String testMethodName) {
        return new InternalMocksContext(logger, testClass, testInstance, testMethodName);
    }

    @Override
    public void visit(final Consumer<InternalMocksContext> element) {
        element.accept(this);
    }

    /**
     * Scan the test instance and register any mock/spy found in the context.
     */
    public void refresh() {
        // Collect the @mock/@spy from the test class
        // instantiate the mocks first, needed to be injected in the spies
        mergeMocks(collectTestInstanceMocks(testClass, testInstance));
        // instantiate the spies
        mergeSpies(collectTestInstanceSpies(testClass, testInstance));
    }

    /**
     * @param mocks The mocks to merge to the {@link InternalMocksContext}.
     */
    public void mergeMocks(final Collection<?> mocks) {
        collectMocksByName(mocks).forEach((mockName, mock) ->
                mockByNames.merge(mockName, mock, (oldMock, newMock) -> newMock)
        );
        collectMocksByClass(mocks).forEach((mockName, clazz) ->
                mockByClasses.merge(mockName, clazz, (oldClass, newClass) -> newClass)
        );
    }

    /**
     * @param spies The spies to merge to the {@link InternalMocksContext}.
     */
    public void mergeSpies(final Map<Field, Object> spies) {
        if (MapUtils.isEmpty(spies)) {
            return;
        }
        spies.keySet().forEach(this::mergeField);
        mergeMocks(spies.values());
    }

    /**
     * @param field The field to merge
     */
    public void mergeField(final Field field) {
        if (field == null) {
            return;
        }
        spyFields.removeIf(entry -> entry.getName().equals(field.getName()));
        spyFields.add(field);
    }

    /**
     * @return  true,   if the Mocks Context has been initialized
     *          false,  otherwise
     */
    public boolean isInitialized() {
        return this.testInstance != null;
    }

    public Class<?> getTestClass() {
        return testInstance.getClass();
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public String getTestMethodName() {
        return testMethodName;
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
        return mockByNames.values().parallelStream().collect(Collectors.toSet());
    }

    /**
     * Get the set of mocks.
     * @return The mock collection
     */
    public Set<Class<?>> getMockClasses() {
        return mockByClasses.values().parallelStream().collect(Collectors.toSet());
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
            mock = findMockByClass(mockClass).stream()
                    // prefer the mock with the exact name, otherwise get any one returned
                    .reduce((a, b) -> {
                        if (MockUtil.getMockSettings(a).getTypeToMock().equals(mockClass)) {
                            return a;
                        }
                        return b;
                    })
                    .orElse(null);
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
     * @param mockClass The mock class to search in the context
     * @return  true, if the context contains the given mock class <br>
     *          false, otherwise
     */
    public boolean contains(final Class<?> mockClass) {
        return mockByClasses.containsValue(mockClass);
    }

    /**
     * Search for a mock in the context by the given class.
     * @param <T>   The mock type
     * @param <E>   Any assignable class
     * @param mockClass The mock class
     * @return The set of mocks
     */
    public <T, E extends T> Set<E> findMockByClass(final Class<T> mockClass) {
        return mockByClasses.entrySet().stream()
            .filter(entry -> entry.getValue().equals(mockClass) || mockClass.isAssignableFrom(entry.getValue()))
            .map(entry -> {
                E mock = (E) mockByNames.get(entry.getKey());
                if (mock == null) {
                    mock = (E) findAssignableMocks(mockClass).stream().findFirst().orElse(null);
                }
                return mock;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * Search for all mocks in the context, that's can be assigned to the given class.
     * @param <T>   The mock type
     * @param <E>   Any assignable class
     * @param mockClass The mock class
     * @return The mocks list
     */
    public <T, E extends T> List<E> findAssignableMocks(final Class<T> mockClass) {
        return mockByClasses.entrySet().parallelStream()
            .filter(entry -> mockClass.isAssignableFrom(entry.getValue()))
            .map(entry -> (E) mockByNames.get(entry.getKey()))
            .collect(Collectors.toList());
    }

    private void logContextState(final Logger logger, final Class<?> testClass, final String testMethodName) {
        if (ObjectUtils.anyNull(logger, testClass)) {
            return;
        }
        final String methodName = ObjectUtils.defaultIfNull(testMethodName, StringUtils.EMPTY);
        logger.debug(MOCKS_CONTEXT_ENABLED_TEMPLATE, methodName, testClass.getSimpleName());
    }

    private static <C extends Collection<?>> Map<String, Object> collectMocksByName(final C mocks) {
        return mocks.parallelStream()
            .filter(MockUtil::isMock)
            .collect(
                Collectors.toMap(
                    mock -> MockUtil.getMockName(mock).toString(),
                    Function.identity(),
                    (oldValue, newValue) -> newValue
                )
            );
    }

    private static Set<Field> collectSpies(final Object testInstance) {
        return ReflectionUtils.getAllFields(
            testInstance.getClass(),
            field -> {
                final Object fieldValue = ReflectionTestUtils.getField(testInstance, field.getName());
                return MockUtil.isSpy(fieldValue);
            }
        );
    }

    private static <C extends Collection<?>> Map<String, Class<?>> collectMocksByClass(final C mocks) {
        return mocks.parallelStream()
            .filter(MockUtil::isMock)
            .collect(
                Collectors.<Object, String, Class<?>>toMap(
                    mock -> MockUtil.getMockName(mock).toString(),
                    mock -> MockUtil.getMockSettings(mock).getTypeToMock(),
                    (oldValue, newValue) -> newValue
                )
            );
    }

    private static List<Object> collectTestInstanceMocks(final Class<?> testClass, final Object testInstance) {
        if (ObjectUtils.anyNull(testClass, testInstance)) {
            return List.of();
        }
        // Instantiate @Mock fields first
        return ReflectionUtils.getAllFields(testClass).stream()
                .map(field -> ReflectionTestUtils.getField(testInstance, field.getName()))
                .filter(ObjectUtils::allNotNull)
                .filter(MockUtil::isMock)
                .collect(Collectors.toList());
    }

    private static Map<Field, Object> collectTestInstanceSpies(final Class<?> testClass, final Object testInstance) {
        if (ObjectUtils.anyNull(testClass, testInstance)) {
            return Map.of();
        }
        // Instantiate @Spy fields
        return ReflectionUtils.getAllFields(testClass).stream()
                .map(field -> Pair.of(field, ReflectionTestUtils.getField(testInstance, field.getName())))
                .filter(pair -> pair.getValue() != null
                        && (MockUtil.isSpy(pair.getValue()) || isCollectionOrArray(pair.getValue(), MockUtil::isSpy))
                )
                // map to Pair<Field, Field value>
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private static boolean isCollectionOrArray(final Object value, final Predicate<Object> itemPredicate) {
        if (Collection.class.isAssignableFrom(value.getClass())) {
            return ((Collection<?>) value).stream().allMatch(itemPredicate);
        } else if (value.getClass().isArray()) {
            return itemPredicate.test(value);
        }
        return false;
    }

}
