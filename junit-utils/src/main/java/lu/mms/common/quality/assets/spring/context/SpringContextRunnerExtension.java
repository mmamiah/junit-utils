package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.JunitUtilsPreconditionException;
import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.no.NoClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.Configurations;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JUnit's extension for {@link SpringContextRunner}. <br>
 * Its instantiate  any field in the related test class, annotated with @{@link SpringContextRunner}, and runs
 * after {@link org.mockito.junit.jupiter.MockitoExtension}. Hence any mock initialized will be add to this application
 * context.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public class SpringContextRunnerExtension extends JunitUtilsExtension
                                        implements BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextRunnerExtension.class);
    private static final PropertyPlaceholderHelper PROPERTY_HELPER = new PropertyPlaceholderHelper("${", "}");

    private static final Set<String> YAML = Set.of("yaml", "yml");

    private static final String FIELD_CLASS_ERROR_FORMAT =
        "The field [%s] is not an instance of 'AbstractApplicationContextRunner'.";

    @Override
    public void beforeEach(final ExtensionContext context) {
        final ExtensionContext.Store store = getStore(context);
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);

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
     * Instantiate the {@link ApplicationContextRunner} field and apply the {@link SpringContextRunner} settings.
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
        if (annotation.withConfiguration() != NoClass.class) {
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

        // set property values
        final Map<String, String> properties = new HashMap<>();
        final PropertySource propertySource = annotation.withPropertySource();
        final Stream<String> propertiesStream;
        final boolean ignoreResourceNotFound;

        // collect the required config files
        if (ArrayUtils.isEmpty(annotation.withActiveProfiles())) {
            propertiesStream = Stream.of(propertySource.value());
            ignoreResourceNotFound = propertySource.ignoreResourceNotFound();
        } else {
            final Stream<String> userEnvConfig = Stream.concat(
                    Stream.of("application.yaml", "application.yml", "application.properties")
                            .filter(appProp -> !ArrayUtils.contains(propertySource.value(), appProp)),
                    Stream.of(annotation.withActiveProfiles())
                            .flatMap(active -> Stream.of(
                                            String.format("application-%s.yaml", active),
                                            String.format("application-%s.yml", active),
                                            String.format("application-%s.properties", active)
                                    )
                            )
            );
            propertiesStream = Stream.concat(
                    Stream.of(propertySource.value()),
                    userEnvConfig
            );
            ignoreResourceNotFound = true;
        }

        // #1 applying the properties from env. config (application.yml) & from 'withPropertySource'
        retrieveProperties(properties, propertiesStream, ignoreResourceNotFound);

        // #2 applying the properties from 'withPropertyValues'
        if (ArrayUtils.isNotEmpty(annotation.withPropertyValues())) {
            Stream.of(annotation.withPropertyValues())
                    .map(value -> {
                        final String[] args = value.split("=");
                        return Map.entry(
                                ArrayUtils.get(args, 0, StringUtils.EMPTY),
                                ArrayUtils.get(args, 1, StringUtils.EMPTY)
                        );
                    })
                    .forEach(entry -> properties.merge(
                            entry.getKey(), entry.getValue(), (oldVal, newVal) -> newVal)
                    );
        }

        // collect the key/value pairs
        final Properties finalProperties = new Properties();
        finalProperties.putAll(properties);

        final String[] pairs =  properties.entrySet().stream()
                .map(entry -> {
                    String value = PROPERTY_HELPER.replacePlaceholders(String.valueOf(entry.getValue()), finalProperties);
                    if (annotation.ignorePropertyEncryption() && value.startsWith("ENC(") && value.endsWith(")")) {
                        value = value.replaceFirst("ENC\\(", value);
                        value = value.substring(0, value.lastIndexOf(")")-1);
                    }
                    return StringUtils.joinWith("=", entry.getKey(), value);
                })
                .toArray(String[]::new);

        atomicRunner.getAndUpdate(appContext -> appContext.withPropertyValues(pairs));

        // #3 applying the properties from 'withSystemProperties'
        if (ArrayUtils.isNotEmpty(annotation.withSystemProperties())) {
            atomicRunner.getAndUpdate(appContext -> appContext.withSystemProperties(annotation.withSystemProperties()));
        }
    }

    private static void retrieveProperties(final Map<String, String> properties, final Stream<String> propertySources,
                                           final boolean ignoreResourceNotFound) {
        propertySources
                // sorting the properties files by name. 'application.yml' to be handled first so that his values
                // are overrided by 'application-test.yml' for example.
                .sorted(SpringContextRunnerExtension::comparePropertySources)
                .forEach(filename -> {
                    final Properties tempProps = retrievePropertiesValues(filename, ignoreResourceNotFound);
                    // overriding the collected properties: 'application.yml' is override.
                    tempProps.forEach((key, value) -> properties.merge(
                            String.valueOf(key), String.valueOf(value), (oldVal, newVal) -> newVal)
                    );
                });
    }

    private static int comparePropertySources(final String p1, final String p2) {
        // compare file name (without extension)
        final String p1Name = FilenameUtils.getBaseName(p1);
        final String p2Name = FilenameUtils.getBaseName(p2);
        if (StringUtils.length(p1Name) < StringUtils.length(p2Name)) {
            return -1;
        } else if (StringUtils.length(p1Name) > StringUtils.length(p2Name)) {
            return 1;
        }
        return 0;
    }

    private static Properties retrievePropertiesValues(final String filename, final boolean ignoreResourceNotFound) {
        final ClassPathResource resource = new ClassPathResource(
                filename, SpringContextRunnerExtension.class.getClassLoader()
        );
        if (!resource.exists()) {
            if (ignoreResourceNotFound) {
                return new Properties();
            }
            final String msg = String.format(
                    "class path resource [%s] cannot be opened because it does not exist", filename
            );
            throw new MissingResourceException(msg, null, null);
        }

        final String extension = FilenameUtils.getExtension(filename);
        final Properties properties;
        if (YAML.contains(extension)) {
            final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource);
            properties = factory.getObject();
        } else {
            try (InputStream inputStream = resource.getInputStream()) {
                properties = new Properties();
                properties.load(inputStream);
            } catch (IOException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }
        return properties;
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
        if (ArrayUtils.isNotEmpty(contextRunnerAnnotation.withActiveProfiles())) {
            applicationContext.getEnvironment().setActiveProfiles(contextRunnerAnnotation.withActiveProfiles());
        }
        try {
            applicationContextRunner = (AbstractApplicationContextRunner<?, ?, ?>) field.getType()
                    .getDeclaredConstructor(Supplier.class)
                    .newInstance((Supplier<?>) () -> applicationContext);

            applicationContextRunner = applicationContextRunner
                // BUGFIX #13: allow bean definition overriding
                .withAllowBeanDefinitionOverriding(contextRunnerAnnotation.withAllowBeanDefinitionOverriding())
                // Due to a [FamilyConfig': Requested bean is currently in creation: Is there an unresolvable
                // circular reference?]. We need to allow this by now. Need to investigate how to get rid of this.
                .withAllowCircularReferences(true)
                .withInitializer(
                    new RunnerApplicationContextInitializer(
                            contextRunnerAnnotation,
                            mocksContext
                    )
                );
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException ex) {
            LOGGER.error("Failed to instantiate the context runner [{}].", field.getType().getSimpleName(), ex);
        }
        return applicationContextRunner;
    }

}
