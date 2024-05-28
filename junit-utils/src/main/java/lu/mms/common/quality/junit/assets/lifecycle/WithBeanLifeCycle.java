package lu.mms.common.quality.junit.assets.lifecycle;


import org.apiguardian.api.API;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.event.annotation.BeforeTestExecution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * {@link WithBeanLifeCycle} annotation, which extends the annotated test class with {@link BeanLifeCycleExtension},
 * extension which execute the Subject Under Test (sut) bean life cycle methods, for test and/or verification purpose.
 * @see BeanLifeCycleExtension
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(BeanLifeCycleExtension.class)
@Documented
@API(
        status = API.Status.STABLE,
        since = "1.0.0"
)
public @interface WithBeanLifeCycle {

    /**
     * Execute the bean life cycle methods at the JUnit5 @{@link BeforeEach} stage.
     * @see BeforeEach
     * @see BeforeTestExecution
     * @return <b>true</b>,  bean lifecycle execution is triggered at @{@link BeforeEach} stage <br>
     *         <b>false</b>, bean lifecycle execution is triggered at @{@link BeforeTestExecution} stage
     */
    boolean beforeEach() default false;

}
