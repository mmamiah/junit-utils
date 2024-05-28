package lu.mms.common.quality.junit.assets.spring.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class updates the bean definition by updating its instance supplier to return the selected mock.
 */
final class MockBeanDefinitionCustomizer implements BeanDefinitionCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockBeanDefinitionCustomizer.class);

    private final ConfigurableListableBeanFactory beanFactory;
    private final Class<?> beanType;
    private final Object declaredMock;

    private MockBeanDefinitionCustomizer(final ConfigurableListableBeanFactory beanFactory,
                                         final Class<?> beanType,
                                         final Object declaredMock) {
        this.beanFactory = beanFactory;
        this.beanType = beanType;
        this.declaredMock = declaredMock;
    }

    static MockBeanDefinitionCustomizer newCustomizer(final ConfigurableListableBeanFactory beanFactory,
                                               final Class<?> beanType, final Object declaredMock) {
        return new MockBeanDefinitionCustomizer(beanFactory, beanType, declaredMock);
    }

    @Override
    public void customize(final BeanDefinition beanDefinition) {
        // collect previous bean definitions
        final Set<AbstractBeanDefinition> beanDefinitions = Stream.of(beanFactory.getBeanNamesForType(beanType))
            .map(beanName -> (AbstractBeanDefinition) beanFactory.getBeanDefinition(beanName))
            .collect(Collectors.toSet());

        // add the submitted bean definition
        beanDefinitions.add((AbstractBeanDefinition) beanDefinition);

        // update the suppliers
        beanDefinitions.forEach(def -> def.setInstanceSupplier(() -> declaredMock));
        LOGGER.debug("Customized [{}] bean definition(s) for bean [{}].", beanDefinitions.size(), beanType);
    }

}
