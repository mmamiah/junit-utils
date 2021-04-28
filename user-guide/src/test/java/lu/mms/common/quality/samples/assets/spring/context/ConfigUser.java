package lu.mms.common.quality.samples.assets.spring.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sample configuration.
 */
@Configuration
public class ConfigUser {

    /**
     * @return the created entity
     */
    @Bean
    public EntityBrown entityBrown() {
        return new EntityBrown();
    }

}
