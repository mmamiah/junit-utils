package lu.mms.common.quality.userguide.spring.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sample configuration.
 */
@Configuration
@EnableConfigurationProperties
public class ConfigUser {

    @Value("${entity.colorName}")
    private String name;

    private UserAccount userAccount;

    public UserAccount getUserAccount() {
        return userAccount;
    }

    @Autowired
    public void setUserAccount(final UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the created entity
     */
    @Bean
    public EntityBrown entityBrown() {
        return new EntityBrown();
    }

}
