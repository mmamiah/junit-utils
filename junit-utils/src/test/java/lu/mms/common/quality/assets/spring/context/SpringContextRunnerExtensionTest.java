package lu.mms.common.quality.assets.spring.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith({MockitoExtension.class, SpringContextRunnerExtension.class})
class SpringContextRunnerExtensionTest {

    @SpringContextRunner(
            withConfiguration = MyConfig.class,
            withMocks = Address.class,
            mappersPackage = "lu.mms.common.quality.assets.spring.context"
    )
    private ApplicationContextRunner contextRunner;

    @Mock(name = "niceCustomer")
    private Customer customerMock;

    @Mock
    private Address addressMock;

    @Test
    void shouldInitializeApplicationContextRunner() {
        // Arrange
        assumeTrue(contextRunner != null);

        // Act
        contextRunner.run(contextAssertProvided -> {
            final Customer customer = contextAssertProvided.getBean(Customer.class);
            assertThat(MockUtil.isMock(customer), equalTo(true));

            final Customer niceCustomer = contextAssertProvided.getBean("niceCustomer", Customer.class);
            assertThat(MockUtil.isMock(niceCustomer), equalTo(true));
        });

        // Assert
        final Boolean canOverride = Optional.of(contextRunner)
                .map(context -> ReflectionTestUtils.getField(context, "runnerConfiguration"))
                .map(config -> String.valueOf(ReflectionTestUtils.getField(config, "allowBeanDefinitionOverriding")))
                .map(Boolean::valueOf)
                .orElse(false);
        // BUGFIX #13
        assertThat(canOverride, equalTo(true));
    }

    @Configuration
    private static class MyConfig {
        private Address address;
        public MyConfig() {
            // default constructor
        }
    }

    // Class to be mock
    private static class Customer {
        private Address address;
        public Address getAddress() {
            return address;
        }
    }

    private static class Address {
        public String getDescription() {
            return "12, Place de la gare";
        }
    }

}
