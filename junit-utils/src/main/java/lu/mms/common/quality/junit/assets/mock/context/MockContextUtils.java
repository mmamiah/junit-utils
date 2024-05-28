package lu.mms.common.quality.junit.assets.mock.context;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.mockito.internal.util.MockUtil;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * This class provides static method that helps to handle mock within the mock context ({@link InternalMocksContext}).
 */
public final class MockContextUtils {

    private MockContextUtils() {
        // hidden constructor
    }

    /**
     * @param klass The class to check
     * @return  true, if the given class is a collection or an array <br>
     *          false, otherwise
     */
    public static boolean isCollectionOrArray(final Class<?> klass) {
        return klass != null && (Collection.class.isAssignableFrom(klass) || klass.isArray());
    }

    /**
     * Retrieve the first generic type from a given class. <br>
     * For example, if klass is an ArrayList of "String", then the returned type will be "String".
     * @param klass The class to check
     * @param type  The {@link ParameterizedType}
     * @return The generic type class
     */
    public static Class<?> retrieveFirstGenericType(final Class<?> klass, final Type type) {
        Class<?> componentType = null;
        if (klass.isArray()) {
            componentType = klass.getComponentType();
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            if (ArrayUtils.isNotEmpty(parameterizedType.getActualTypeArguments())) {
                componentType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return componentType;
    }

    /**
     * Return the collected mocks into the desired type.
     * @param type  The desired mocks collection or array type
     * @param mocks The list of mocks to return
     * @return  The typed collection/array of mocks
     */
    public static Object retrieveMocksCollectionWithSafeType(final Class<?> type, final List<?> mocks) {

        if (List.class.isAssignableFrom(type)) {
            return mocks;
        }

        final List<?> safeMocks = ObjectUtils.defaultIfNull(mocks, List.of());
        final Object mocksCollection;
        if (Set.class.isAssignableFrom(type)) {
            mocksCollection = new HashSet<>(safeMocks);
        } else {
            mocksCollection = new ModelMapper().map(safeMocks, type);
        }
        return mocksCollection;
    }

    /**
     * Retrieve a class constructors arguments from the mocks context.
     * @param mocksContext  The mock context
     * @param parameters    The parameters
     * @return              The constructor argument value
     */
    public static Object[] retrieveMocksByParameters(final InternalMocksContext mocksContext,
                                                     final Parameter[] parameters) {
        Objects.requireNonNull(mocksContext, "The mocks context should be non null.");
        final Object[] args = new Object[parameters.length];
        // retrieve the mocks matching to the method parameters
        IntStream.range(0, parameters.length).forEach(index -> {
            final Parameter param = parameters[index];
            final Object mock;
            if (isCollectionOrArray(param.getType())) {
                // handling  Collections and Arrays
                final Class<?> componentType = retrieveFirstGenericType(param.getType(), param.getParameterizedType());
                final List<?> candidates = mocksContext.findAssignableMocks(componentType);
                mock = retrieveMocksCollectionWithSafeType(param.getType(), candidates);
            } else {
                mock = mocksContext.findMockByNameOrClass(param.getName(), param.getType());
            }
            // collecting the method arguments (mocks)
            args[index] = mock;
        });
        return args;
    }

    /**
     * This method returns the class object. <br>
     * If the object is a mock, it returns the original object class, otherwise it return the <i>object.getClass()</i>
     * value.
     * @param object The object to check
     * @return  The object class
     */
    public static Class<?> getObjectClass(final Object object) {
        final Class<?> resultClass;
        if (MockUtil.isMock(object)) {
            resultClass = MockUtil.getMockSettings(object).getTypeToMock();
        } else {
            resultClass = object.getClass();
        }
        return resultClass;
    }

}
