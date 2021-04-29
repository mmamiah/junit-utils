package lu.mms.common.quality.assets.spring.context;

import lu.mms.common.quality.assets.None;
import org.apiguardian.api.API;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;

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
    since = "0.0.1"
)
public @interface SpringContextRunner {

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
     * Register the specified configuration class with the ApplicationContext.
     * <b>see:</b> org.springframework.boot.context.annotation.Configurations
     * @return The configuration classes
     */
    Class<?> withConfiguration() default None.class;

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
