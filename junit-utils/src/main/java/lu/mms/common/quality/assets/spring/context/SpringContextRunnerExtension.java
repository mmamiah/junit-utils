package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.JunitUtilsPreconditionException;
import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.None;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.reinforcement.MockReinforcementHandler;
import lu.mms.common.quality.assets.mock.reinforcement.ReinforceMockExtension;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockito.internal.configuration.DefaultInjectionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.Configurations;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JUnit extension for {@link SpringContextRunner}. <br>
 * Its instantiate  any field in the related test class, annotated with @{@link SpringContextRunner}, and runs
 * after {@link org.mockito.junit.jupiter.MockitoExtension}. Hence any mock initialized will be add to this application
 * context.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "3.0.0"
)
public class SpringContextRunnerExtension extends JunitUtilsExtension
                                        implements BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextRunnerExtension.class);

    private static final String FIELD_CLASS_ERROR_FORMAT =
        "The field [%s] is not an instance of 'AbstractApplicationContextRunner'.";

    /** A mock reinforcement handler that do nothing. */
    private static final MockReinforcementHandler DEFAULT_REINFORCEMENT_HANDLER = (a, b, c) -> {};

    private MockReinforcementHandler mockReinforcementHandler;

    @Override
    public void beforeEach(final ExtensionContext context) {
        final ExtensionContext.Store store = getStore(context);
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);
        if (BooleanUtils.isTrue(store.get(ReinforceMockExtension.class, Boolean.class))) {
            this.mockReinforcementHandler = store.get(MockReinforcementHandler.class, MockReinforcementHandler.class);
        }
        if (this.mockReinforcementHandler == null) {
            this.mockReinforcementHandler = DEFAULT_REINFORCEMENT_HANDLER;
        }

        mocksContext.visit(this);
    }

    @Override
    public void beforeTestExecution(final ExtensionContext context) {
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);
        mocksContext.refresh();
        mocksContext.visit(this);
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }

    /**
     * Instantiate the {@link ApplicationContextRunner} field and apply the {@link SpringContextRunner} settings
     * @param mocksContext The test instance mocks context
     */
    @Override
    public void accept(final InternalMocksContext mocksContext) {
        // Check the test class fields
        final List<Field> contextFields = AnnotationSupport.findAnnotatedFields(
                mocksContext.getTestClass(), SpringContextRunner.class);
        if (CollectionUtils.isEmpty(contextFields)) {
            LOGGER.warn("No ApplicationContextRunner field found in the test class.");
            return;
        }

        final Object testInstance = mocksContext.getTestInstance();

        for (Field field: contextFields) {
            // validate that the field is an instance of AbstractApplicationContextRunner.
            if (!AbstractApplicationContextRunner.class.isAssignableFrom(field.getType())) {
                throw new JunitUtilsPreconditionException(String.format(FIELD_CLASS_ERROR_FORMAT, field.getName()));
            }

            final AbstractApplicationContextRunner<?, ?, ?> appRunner = retrieveAppContextRunner(mocksContext, field);

            if (appRunner == null) {
                LOGGER.warn("The ApplicationContextRunner field [{}] is not instantiated skipping it.", field.getName());
                continue;
            }

            // instantiate the context if not yet instantiated, and apply the configuration
            final AbstractApplicationContextRunner<?, ?, ?> contextRunner = applyContextRunnerConfig(field, appRunner);

            // re-inject the updated class runner.
            ReflectionTestUtils.setField(testInstance, field.getName(), contextRunner);
        }

    }

    /**
     * @param needingInjection Fields needing mock injection
     * @param mocks The mocks to inject
     * @param ofInstance Instance owning the <code>field</code>
     */
    public void injectMocksOnFields(Set<Field> needingInjection, Set<Object> mocks, Object ofInstance) {
        new DefaultInjectionEngine().injectMocksOnFields(needingInjection, mocks, ofInstance);
    }

    private AbstractApplicationContextRunner<?, ?, ?> retrieveAppContextRunner(
                                                                            final InternalMocksContext mocksContext,
                                                                            final Field field) {
        if (ReflectionTestUtils.getField(mocksContext.getTestInstance(), field.getName()) != null) {
            return (AbstractApplicationContextRunner<?, ?, ?>) ReflectionTestUtils.getField(
                    mocksContext.getTestInstance(),
                    field.getName()
            );
        }
        return newContextRunnerInstance(field, mocksContext);
    }

    private static AbstractApplicationContextRunner<?, ?, ?> applyContextRunnerConfig(final Field field,
                                                   final AbstractApplicationContextRunner<?, ?, ?> appContextRunner) {
        final AtomicReference<AbstractApplicationContextRunner<?, ?, ?>> atomicRunner = new AtomicReference<>();
        atomicRunner.set(appContextRunner);

        // Initialize the contextRunner with configuration defined in the annotation
        final SpringContextRunner contextRunnerAnnotation = field.getAnnotation(SpringContextRunner.class);

        withConfiguration(atomicRunner, contextRunnerAnnotation);
        withProperties(atomicRunner, contextRunnerAnnotation);
        // The mocks are initialized in the ApplicationContextInitializer.
        withBeans(atomicRunner, contextRunnerAnnotation);
        return atomicRunner.get();
    }

    private static void withConfiguration(final AtomicReference<AbstractApplicationContextRunner<?, ?, ?>> atomicRunner,
                                          final SpringContextRunner annotation) {
        // Context configuration
        if (annotation.withConfiguration() != None.class) {
            final Configurations configurations = AutoConfigurations.of(annotation.withConfiguration());
            atomicRunner.getAndUpdate(appContext -> appContext.withConfiguration(configurations));
        }

        // set user configurations
        if (ArrayUtils.isNotEmpty(annotation.withUserConfiguration())) {
            atomicRunner.getAndUpdate(appContext -> appContext
                .withUserConfiguration(annotation.withUserConfiguration()));
        }
    }

    private static void withProperties(final AtomicReference<AbstractApplicationContextRunner<?, ?, ?>> atomicRunner,
                                       final SpringContextRunner annotation) {
        // set properties value
        if (ArrayUtils.isNotEmpty(annotation.withPropertyValues())) {
            atomicRunner.getAndUpdate(appContext -> appContext.withPropertyValues(annotation.withPropertyValues()));
        }

        // set system properties
        if (ArrayUtils.isNotEmpty(annotation.withSystemProperties())) {
            atomicRunner.getAndUpdate(appContext -> appContext.withSystemProperties(annotation.withSystemProperties()));
        }
    }

    /**
     * Add relevant classes as bean in the context.
     * @param atomicRunner the atomic application context runner
     * @param annotation the {@link SpringContextRunner} annotation instance
     */
    private static void withBeans(final AtomicReference<AbstractApplicationContextRunner<?, ?, ?>> atomicRunner,
                                  final SpringContextRunner annotation) {
        // set properties value
        Stream.of(annotation.withBeans())
            .forEach(bean -> atomicRunner.getAndUpdate(appContext -> appContext.withBean(bean)));
    }

    /**
     * Produce a new instance of {@link AbstractApplicationContextRunner}.
     * @param field the field annotated with {@link SpringContextRunner}
     * @param mocksContext the test mocks context
     * @return the application context runner object
     */
    private AbstractApplicationContextRunner<?, ?, ?> newContextRunnerInstance(final Field field,
                                                                               final InternalMocksContext mocksContext) {

        // init the bean factory initializer
        final SpringContextRunner contextRunnerAnnotation = field.getAnnotation(SpringContextRunner.class);

        // build the Application Context Runner
        AbstractApplicationContextRunner<?, ?, ?> applicationContextRunner = null;
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        try {
            applicationContextRunner = (AbstractApplicationContextRunner<?, ?, ?>) field.getType()
                    .getDeclaredConstructor(Supplier.class)
                    .newInstance((Supplier<?>) () -> applicationContext);

            applicationContextRunner = applicationContextRunner
                // BUGFIX #13: allow bean definition overriding
                .withAllowBeanDefinitionOverriding(contextRunnerAnnotation.withAllowBeanDefinitionOverriding())
                .withInitializer(
                    new RunnerApplicationContextInitializer(
                            contextRunnerAnnotation,
                            mocksContext,
                            this.mockReinforcementHandler
                    )
                );
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException ex) {
            LOGGER.error("Failed to instantiate the context runner [{}].", field.getType().getSimpleName(), ex);
        }
        return applicationContextRunner;
    }

}
