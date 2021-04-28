package lu.mms.common.quality.assets.lifecycle;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.unittest.UnitTest;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * JUnit extension to execute the test instance <b>non-mock</b> members lifecycle methods. <br>
 * Any @Mock test instance member will be enforced/fulfilled in order to avoid NPE when executing lifecycle methods.
 * @see PostConstruct
 * @see PreDestroy
 * @see InitializingBean
 * @see DisposableBean
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "3.2.0"
)
public class BeanLifeCycleExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanLifeCycleExtension.class);

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        final UnitTest unitTest = extensionContext.getRequiredTestClass().getDeclaredAnnotation(UnitTest.class);
        if (unitTest != null && !unitTest.lifeCycle()) {
            return;
        }
        executeLifeCycleClass(extensionContext.getRequiredTestInstance(), InitializingBean.class);
        executeLifeCycleAnnotation(extensionContext, PostConstruct.class);
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final UnitTest unitTest = extensionContext.getRequiredTestClass().getDeclaredAnnotation(UnitTest.class);
        if (unitTest != null && !unitTest.lifeCycle()) {
            return;
        }
        executeLifeCycleClass(extensionContext.getRequiredTestInstance(), DisposableBean.class);
        executeLifeCycleAnnotation(extensionContext, PreDestroy.class);
    }

    private static void executeLifeCycleClass(final Object testInstance, final Class<?> lifeCycleClass) {
        ReflectionUtils.getAllFields(
                testInstance.getClass(),
                field -> lifeCycleClass.isAssignableFrom(field.getType())
        ).forEach(field -> {
            final Method method = ReflectionUtils.getAllMethods(lifeCycleClass).stream().findFirst().orElse(null);
            executeMethod(testInstance, field.getName(), method.getName());
        });
    }

    private static void executeLifeCycleAnnotation(final ExtensionContext context,
                                                   final Class<? extends Annotation> annotationClass) {
        final Object testInstance = context.getRequiredTestInstance();
        // recover the matching field from the relevant test class
        ReflectionUtils.getAllFields(testInstance.getClass())
            .stream()
            // get the test instance fields and their methods annotated with @PostConstruct.
            .map(field -> mapFieldMethods(field, annotationClass))
            // Filter test instance properties (Classes) having target methods (@PostConstruct)
            .filter(entry -> !entry.getValue().isEmpty())
            // for each entry execute the Lifecycle annotation method (@PostConstruct / @PreDestroy).
            .forEach(entry -> entry.getValue()
                    .forEach(method -> executeMethod(testInstance, entry.getKey(), method.getName()))
            );
    }

    private static Map.Entry<String, List<Method>> mapFieldMethods(final Field field,
                                                            final Class<? extends Annotation> annotationClass) {
        final List<Method> methods = AnnotationSupport.findAnnotatedMethods(
            field.getType(),
            annotationClass,
            HierarchyTraversalMode.TOP_DOWN
        );
        return Map.entry(field.getName(), methods);
    }

    private static void executeMethod(final Object testInstance, final String fieldName, final String methodName) {
        final Object fieldValue = ReflectionTestUtils.getField(testInstance, fieldName);
        if (fieldValue == null || (MockUtil.isMock(fieldValue) && !MockUtil.isSpy(fieldValue))) {
            LOGGER.warn("Skipping property [{}]", fieldName);
        } else {
            ReflectionTestUtils.invokeMethod(fieldValue, methodName);
            LOGGER.debug("[{}].[{}] has been executed", fieldName, methodName);
        }
    }

}
