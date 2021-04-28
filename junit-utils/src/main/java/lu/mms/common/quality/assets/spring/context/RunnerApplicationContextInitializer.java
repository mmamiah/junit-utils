package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.reinforcement.MockReinforcementHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Mapper;
import org.mockito.internal.util.MockUtil;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lu.mms.common.quality.assets.mock.context.MockContextUtils.getObjectClass;
import static lu.mms.common.quality.utils.FrameworkAnnotationUtils.buildReflections;
import static org.mockito.Mockito.mock;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * This class initialize the bean factory with user mocks as per the {@link SpringContextRunner} configuration.
 * It synchronize the mocks declared by the user with the one required after bean factory refresh and initialization.
 */
class RunnerApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerApplicationContextInitializer.class);

    private final SpringContextRunner contextRunnerAnnotation;
    private final InternalMocksContext mocksContext;
    private final MockReinforcementHandler mockReinforcementHandler;

    /**
     * @param contextRunnerAnnotation The context runner annotation
     * @param mocksContext The test instance mocks context
     * @param mockReinforcementHandler The mock reinforcement handler
     */
    RunnerApplicationContextInitializer(final SpringContextRunner contextRunnerAnnotation,
                                        final InternalMocksContext mocksContext,
                                        final MockReinforcementHandler mockReinforcementHandler) {
        this.contextRunnerAnnotation = contextRunnerAnnotation;
        this.mocksContext = mocksContext;
        this.mockReinforcementHandler = mockReinforcementHandler;
    }

    @Override
    public void initialize(final ConfigurableApplicationContext context) {

        // initialize the mocks before completing application context initialization
        mockBeanFactoryInitialization(context.getBeanFactory());

    }

    /**
     * Initializes the BeanFactory with mocks.
     * @param beanFactory The bean factory
     */
    private void mockBeanFactoryInitialization(final ConfigurableListableBeanFactory beanFactory) {
        if (contextRunnerAnnotation.injectDeclaredMocks()) {
            // register declared mocks if relevant
            mocksContext.getMocks().forEach(mock -> synchronizeBeanDefinition(beanFactory, mock));
        }

        final Set<Object> mocks = new HashSet<>(mocksContext.getMocks());

        // register 'withMocks' classes if package declared
        mocks.addAll(registerMocks(beanFactory, contextRunnerAnnotation.withMocks()));

        // mock mappers if package declared
        mocks.addAll(
            Stream.of(contextRunnerAnnotation.mappersPackage())
                .flatMap(targetPackage -> {
                    final Reflections reflections = buildReflections(targetPackage);
                    // retrieve the mappers list
                    final Set<Class<?>> mappersClasses = reflections.getTypesAnnotatedWith(Mapper.class);

                    return registerMocks(beanFactory, mappersClasses.toArray(Class[]::new)).stream();
                })
                .collect(Collectors.toSet())
        );

        // For consistency, ensure that any config class is initialized with mocks/spies if relevant
        Stream.concat(
            Stream.of(contextRunnerAnnotation.withUserConfiguration()),
            Stream.of(contextRunnerAnnotation.withConfiguration())
        ).flatMap(configClass -> ReflectionUtils.getAllFields(configClass).stream())
        .map(field -> Pair.of(field, toNamedBeanHolder(beanFactory, field.getDeclaringClass())))
        .filter(pair -> Objects.nonNull(pair.getValue()))
        .forEach(pair -> {
            final Field field = pair.getKey();
            final Object beanInstance = pair.getValue().getBeanInstance();

            final Object mock = mocksContext.findMockByField(field);
            if (mock != null) {
                ReflectionTestUtils.setField(beanInstance, field.getName(), mock);
                return;
            }
            mockReinforcementHandler.injectMocksOnFields(Set.of(field), mocks, beanInstance);
        });

    }

    private static NamedBeanHolder<?> toNamedBeanHolder(final ConfigurableListableBeanFactory beanFactory,
                                                        final Class<?> configClass) {
        try {
            return beanFactory.resolveNamedBean(configClass);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    private Set<Object> registerMocks(final ConfigurableListableBeanFactory beanFactory, final Class<?>... classesToMock) {
        return Stream.of(classesToMock)
            .map(classToMock -> synchronizeBeanDefinition(beanFactory, classToMock))
            .collect(Collectors.toSet());
    }

    private Object synchronizeBeanDefinition(final ConfigurableListableBeanFactory beanFactory, final Class<?> mockClass) {
        final String beanName = StringUtils.uncapitalize(mockClass.getSimpleName());
        final String[] beanNames = beanFactory.getBeanNamesForType(mockClass);
        final Object mock;
        if (ArrayUtils.contains(beanNames, beanName)) {
            // The bean is already defined, no need to register it again
            return null;
        } else if (mocksContext.contains(mockClass)) {
            mock = mocksContext.findMockByNameOrClass(beanName, mockClass);
        } else {
            mock = mock(mockClass);
        }
        synchronizeBeanDefinition(beanFactory, mock);
        return mock;
    }

    private void synchronizeBeanDefinition(final ConfigurableListableBeanFactory beanFactory, final Object object) {
        final Class<?> mockClass = getObjectClass(object);

        // check if the bean exists, and remove his definition
        final String[] beanNames = beanFactory.getBeanNamesForType(mockClass);
        if (ArrayUtils.isEmpty(beanNames)) {
            // register bean if it isn't yet defined
            registerBeanDefinition(beanFactory, object);
        } else {
            // set it's supplier if bean already defined
            Stream.of(beanNames).forEach(beanName -> {
                if (beanFactory.containsBeanDefinition(beanName)) {
                    // ensure that returned bean is the selected mock.
                    final AbstractBeanDefinition def = (AbstractBeanDefinition) beanFactory.getBeanDefinition(beanName);
                    if (def.getInstanceSupplier() == null) {
                        def.setInstanceSupplier(() -> object);
                    }
                } else {
                    LOGGER.warn("Trying to customize a non registered bean. The bean [{}] name was found in "
                        + "BeanFactory, but it is still missing in the Application context.", beanName);
                    // register bean if it isn't yet defined
                    registerBeanDefinition(beanFactory, object);
                }
            });
        }
    }

    private static <T> void registerBeanDefinition(final ConfigurableListableBeanFactory beanFactory, final Object mock) {
        final BeanDefinitionRegistry beanRegistry = (BeanDefinitionRegistry) beanFactory;

        final String mockName = MockUtil.getMockName(mock).toString();
        final Class<T> mockClass = MockUtil.getMockSettings(mock).getTypeToMock();
        final BeanDefinitionBuilder defBuilder = genericBeanDefinition(mockClass, () -> (T) mock);
        beanRegistry.registerBeanDefinition(mockName, defBuilder.getBeanDefinition());

        final String beanName = StringUtils.uncapitalize(mockClass.getSimpleName());
        if (!beanName.equals(mockName)) {
            beanRegistry.registerAlias(mockName, beanName);
        }
    }

}
