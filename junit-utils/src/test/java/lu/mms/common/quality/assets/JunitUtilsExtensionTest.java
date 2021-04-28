package lu.mms.common.quality.assets;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mybatis.MyBatisExtension;
import lu.mms.common.quality.assets.spring.context.SpringContextRunnerExtension;
import lu.mms.common.quality.assets.unittest.UnitTest;
import lu.mms.common.quality.junit.platform.SpiConfiguration;
import lu.mms.common.quality.utils.FrameworkAnnotationUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
public class JunitUtilsExtensionTest {

    /** Keep the declared order as the declared method will be called in this order. */
    private static final Set<Class<? extends Extension>> CALLBACKS_EXTENSION = Set.of(
            BeforeAllCallback.class,
            BeforeEachCallback.class,
            BeforeTestExecutionCallback.class,
            AfterTestExecutionCallback.class,
            AfterEachCallback.class,
            AfterAllCallback.class
    );

    /** All extensions included by default. */
    private static final Collection<Class<? extends Extension>> INCLUDED = Collections.singleton(MockitoExtension.class);

    /** All extensions excluded by default. */
    private static final Collection<Class<? extends Extension>> EXCLUDED = Arrays.asList(MyBatisExtension.class,
            SpringContextRunnerExtension.class, JunitUtilsExtension.class);

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    private ExtensionContext.Store storeSpy = new NamespaceAwareStore(new ExtensionValuesStore(null), JUNIT_UTILS_NAMESPACE);

    final AtomicReference<Supplier<?>> verifyTestMethodContext = new AtomicReference<>(() -> true);

    @BeforeEach
    void init(final TestInfo testInfo) {
        // Creating a lenient SpyAssetVisitor
        storeSpy = mock(
                storeSpy.getClass(),
                withSettings().spiedInstance(storeSpy).defaultAnswer(CALLS_REAL_METHODS).lenient()
        );
        when(extensionContextMock.getStore(eq(JUNIT_UTILS_NAMESPACE))).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @Test
    void shouldValidateExtensionAssociatedWithThisAnnotation() {
        // Arrange
        final Reflections reflections = FrameworkAnnotationUtils.buildReflections(SpiConfiguration.ROOT_PACKAGE);
        final Set<Class<? extends Extension>> extensions = reflections.getSubTypesOf(Extension.class)
                .stream()
                .filter(klass -> klass.getPackageName().contains(SpiConfiguration.ROOT_PACKAGE))
                .collect(Collectors.toSet());

        // Adding/remove default extension
        assumeTrue(extensions.addAll(INCLUDED), "Failed to add default classes");
        assumeTrue(extensions.removeAll(EXCLUDED), "Failed to remove default classes");

        // Act
        final ExtendWith extendWith = UnitTest.class.getAnnotation(ExtendWith.class);

        // Assert
        assertThat(extendWith, notNullValue());
        assertThat(Arrays.asList(extendWith.value()), containsInAnyOrder(extensions.toArray()));
    }

    /**
     * This test case is to ensure that for each create resource is properly cleaned/removed.
     * @param extensionClass    The JUnit Utils extension under test
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("extensionClassProvider")
    void shouldCreateAndCleanMockContextAccordingly(final String simpleName, final Class<?> extensionClass, final TestInfo testInfo) {
        // Arrange
        final JunitUtilsExtension sut = BeanUtils.instantiateClass(extensionClass, JunitUtilsExtension.class);

        final AtomicReference<Supplier<?>> verifyClassContext = new AtomicReference<>(() -> true);
        doAnswer(inv -> {
            // Will be used to assert at the end of the test
            verifyClassContext.set(() -> {
                verify(storeSpy).remove(extensionClass);
                return true;
            });
            return null;
        }).when(storeSpy).put(eq(extensionClass), any(Boolean.class));

        final String testMethodName = testInfo.getTestMethod().map(Method::getName).orElse(null);
        doAnswer(inv -> {
            // Will be used to assert at the end of the test
            verifyTestMethodContext.set(() -> {
                verify(storeSpy).remove(testMethodName, InternalMocksContext.class);
                return true;
            });
            return null;
        }).when(storeSpy).put(eq(testMethodName), any(InternalMocksContext.class));

        // Act
        invokeExtensionMethods(extensionClass, sut, extensionContextMock);

        // Assert
        verifyTestMethodContext.get().get();
        verifyClassContext.get().get();
    }

    private static Stream<Arguments> extensionClassProvider() {
        return Stream.of(UnitTest.class.getAnnotation(ExtendWith.class).value())
                .filter(clazz -> !clazz.equals(MockitoExtension.class))
                .map(clazz -> Arguments.of(clazz.getSimpleName(), clazz));
    }

    private static void invokeExtensionMethods(final Class<?> targetClass, final JunitUtilsExtension sut,
                                               final ExtensionContext extensionContextMock) {
        CALLBACKS_EXTENSION.stream()
            // collect all callback methods
            .flatMap(callbackClass -> Stream.of(callbackClass.getDeclaredMethods()))
            // keep the method which accept 'ExtensionContext' only
            .filter(method -> method.getParameterCount() == 1
                    && List.of(method.getParameterTypes()).contains(ExtensionContext.class)
            )
            // retrieve related method from the class under test
            .map(method -> MethodUtils.getMatchingAccessibleMethod(targetClass, method.getName(), ExtensionContext.class))
            // ensure the method exist in the class under test
            .filter(Objects::nonNull)
            // invoke each method with the 'extensionContextMock' as paramter
            .forEach(method -> {
                try {
                    method.invoke(sut, extensionContextMock);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
    }

}
