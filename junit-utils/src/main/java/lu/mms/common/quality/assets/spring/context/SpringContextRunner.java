package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.assets.no.NoClass;
import org.apiguardian.api.API;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for {@link AbstractApplicationContextRunner}. <br>
 * Used together with the {@link SpringContextRunnerExtension}, it instantiates the annotated field and initializes it
 * with the provided configuration. Any mock initialized will be added as well to the Application context.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public @interface SpringContextRunner {

    /**
     * Specify the set of profiles active for this Environment. <br>
     * See also: {@link org.springframework.test.context.ActiveProfiles}.
     * @return The array of properties to set.
     */
    String[] withActiveProfiles() default {};

    /**
     * In case the Encryption causees some issue, we can turn it off.. <br>
     * @return The flag to turn on/off the property encryption handling.
     */
    boolean ignorePropertyEncryption() default false;

    // Bugfix #13
    /**
     *  Turn spring overriding on/off.
     * @see <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.1-Release-Notes">Spring Boot 2.1
     * Release Notes</a>
     * @return  True, allow bean definition overriding <br>
     *          False, otherwise
     */
    boolean withAllowBeanDefinitionOverriding() default true;

    /**
     * Add the specified Environment property pairs. <br>
     * Key-value pairs can be specified with colon (":") or equals("=") separators.
     * Override matching keys that might have been specified previously.
     * @return The key/value properties.
     */
    String[] withPropertyValues() default {};

    /**
     * Add the specified System property pairs. <br>
     * Key-value pairs can be specified with colon (":") or equals("=") separators.
     * System properties are added before the context is run and restored when the context is closed.
     * @return The key/value System properties.
     */
    String[] withSystemProperties() default {};

    /**
     * Add the specified property source config (.properties, .yaml, .yml). <br>
     * In case multiple properties files are provided, they will be sorted (natural order of their filename length)
     * and applied, the next file override the previous one. <br>
     * Example for <code>application-xxx.yaml</code> and <code>application.yaml</code>, the order will be:
     * <ol>
     *     <li><code>application.yaml</code></li>
     *     <li><code>application-xxx.yaml</code></li>
     * </ol>
     * @return The property source config.
     */
    PropertySource withPropertySource() default @PropertySource({});

    /**
     * Register the specified configuration class with the ApplicationContext.
     * <b>see:</b> org.springframework.boot.context.annotation.Configurations
     * @return The configuration classes
     */
    Class<?> withConfiguration() default NoClass.class;

    /**
     * Register the specified user configuration classes with the ApplicationContext.
     * @return The user configuration classes
     */
    Class<?>[] withUserConfiguration() default {};

    /**
     * Register the specified user beans with the ApplicationContext.
     * @return The user Beans classes to include in the Application context
     */
    Class<?>[] withBeans() default {};

    /**
     * Register the specified classes as mocks with the ApplicationContext.
     * @return The user Beans classes to mock and include in the Application context
     */
    Class<?>[] withMocks() default {};

    /**
     * Register the mocks and spies declared in the test class.
     * @return  true, the mocks / spies declared by the user are injected in the context <br>
     *          false, otherwise
     */
    boolean injectDeclaredMocks() default true;

    /**
     * Mock the mappers under given packages.
     * The package will be scan and all underlining mappers will be mocked.
     * @return The mapper's package to scan
     */
    String[] mappersPackage() default {};

}
