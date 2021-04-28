package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.mockito.internal.stubbing.defaultanswers.ReturnsMocks;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.isCollectionOrArray;
import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveFirstGenericType;
import static lu.mms.common.quality.assets.mock.context.MockContextUtils.retrieveMocksCollectionWithSafeType;

/**
 * Mock invocation answer to return @Mock object if exist in our test declaration.
 */
public class ReturnsMocksAnswer extends ReturnsMocks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnsMocksAnswer.class);

    private final InternalMocksContext mocksContext;

    /**
     * AnnotatedMockAnswer constructor.
     * @param mocksContext The mocks context.
     */
    ReturnsMocksAnswer(final InternalMocksContext mocksContext) {
        this.mocksContext = mocksContext;
    }

    @Override
    public Object answer(final InvocationOnMock invocation) throws Throwable {
        final Method method = invocation.getMethod();
        final Class<?> targetType = method.getReturnType();
        Object mock = retrieveMockFromContext(invocation, method, targetType);

        if (mock == null) {
            // if no mocks found, we can therefore request for a brand new mock.
            mock = super.answer(invocation);
        }

        if (!method.getName().equals("toString")) { // logging all but not 'toString()' method
            LOGGER.warn("Mock [{}] invocation not identified. Returning a new {}", method.getName(), mock);
        }
        return mock;
    }

    private Object retrieveMockFromContext(final InvocationOnMock invocation, final Method method,
                                           final Class<?> targetType) {
        Object mock = null;
        // handling  Collections and Arrays
        if (isCollectionOrArray(targetType)) {
            final Class<?> componentType = retrieveFirstGenericType(targetType, method.getGenericReturnType());
            final List<?> candidates = mocksContext.findAssignableMocks(componentType);
            if (!CollectionUtils.isEmpty(candidates)) {
                mock = retrieveMocksCollectionWithSafeType(targetType, candidates);
            }
        }

        // handling  regular mocks
        if (mocksContext.contains(invocation.getMock())) {
            // retrieve the mock for the expected return type
            final List<?> candidateMocks = mocksContext.findAssignableMocks(targetType);
            if (!CollectionUtils.isEmpty(candidateMocks)) {
                mock = retrieveMock(invocation, candidateMocks);
            }
        }
        return mock;
    }

    private <A> Object retrieveMock(final InvocationOnMock invocation, final List<A> candidateMocks) {
        final Object chosenMock = candidateMocks.parallelStream()
            .filter(mock -> hasField(
                invocation.getMock(),
                // get invocation mock name
                MockUtil.getMockSettings(invocation.getMock()).getMockName().toString(),
                // get the candidate mock name
                MockUtil.getMockName(mock).toString())
            )
            .findAny()
            .orElseGet(() -> {
                final A object = candidateMocks.get(0);
                final String mockName = MockUtil.getMockName(object).toString();
                LOGGER.warn("No matching mock found. The mock [{}] will be returned !", mockName);
                return object;
            });
        LOGGER.info("Mock [{}] invocation identified. Returning [{}]", invocation.getMethod().getName(),
                MockUtil.getMockName(chosenMock));
        return chosenMock;
    }

    private static boolean hasField(final Object target, final String targetDescription, final String fieldName) {
        try {
            ReflectionTestUtils.getField(target, fieldName);
            return true;
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("The field [{}] do not contain the field '{}'", targetDescription, fieldName);
        }
        return false;
    }

}
