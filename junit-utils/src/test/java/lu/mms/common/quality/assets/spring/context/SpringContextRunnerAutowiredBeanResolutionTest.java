package lu.mms.common.quality.assets.spring.context;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith({MockitoExtension.class, SpringContextRunnerExtension.class})
class SpringContextRunnerAutowiredBeanResolutionTest {

    @SpringContextRunner(
            withMocks = Relative.class,
            withUserConfiguration = FamilyConfig.class
    )
    private ApplicationContextRunner appContextRunner;

    @BeforeEach
    void init() {
        assumeTrue(appContextRunner != null, "The <appContextRunner> cannot be null");
    }

    @Test
    void shouldInjectMockDefinedInOutTestInTheApplicationContextWhenSimpleContext() {
        // Arrange
        assumeTrue(appContextRunner != null, "the ApplicationContext is not initialized");

        // Act
        appContextRunner.run(assertProvider -> {
            // Assert
            // This entity is declared in the User configuration
//            assertThat(assertProvider).doesNotHaveBean(Relative.class);

            final FamilyConfig familyConfig = assertProvider.getBean(FamilyConfig.class);
            MatcherAssert.assertThat(familyConfig.getCousin(), notNullValue());
            MatcherAssert.assertThat(familyConfig.getFamilyMember(), notNullValue());
        });
    }

    @Configuration
    private static class FamilyConfig {

        public FamilyConfig() {
            //Default constructor
        }

        @Autowired
        private Relative cousin;

        @Autowired
        private FamilyMember familyMember;

        @Bean
        public FamilyMember getBestCousin() {
            return new FamilyMember();
        }

        public Relative getCousin() {
            return cousin;
        }

        public FamilyMember getFamilyMember() {
            return familyMember;
        }
    }

    @Component
    private static class FamilyMember {

        public FamilyMember() {
            //Default constructor
        }

        @Autowired
        private Relative relative;

        public Relative getRelative() {
            return relative;
        }
    }

    private static class Relative {
        private String name = RandomStringUtils.randomAlphabetic(5);

        public Relative() {
            //Default constructor
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}