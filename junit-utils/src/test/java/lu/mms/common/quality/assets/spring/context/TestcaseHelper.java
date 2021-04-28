package lu.mms.common.quality.assets.spring.context;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

final class TestcaseHelper {

    private final ExtensionContext extensionContextMock;

    private TestcaseHelper(final ExtensionContext extensionContextMock) {
        this.extensionContextMock = extensionContextMock;
    }

    public static TestcaseHelper newHelper(final ExtensionContext extensionContextMock) {
        return new TestcaseHelper(extensionContextMock);
    }

    public <T> T prepareTestCaseMock(final Class<T> clazz, final Method method) {
        final T instance = ReflectionUtils.newInstance(clazz);
        prepareTestCaseMock(clazz, instance, method);
        return instance;
    }

    public void prepareTestCaseMock(final Class clazz, final Object instance, final Method method) {
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(instance);
        when(extensionContextMock.getRequiredTestClass()).thenReturn(clazz);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(method);
    }
}
