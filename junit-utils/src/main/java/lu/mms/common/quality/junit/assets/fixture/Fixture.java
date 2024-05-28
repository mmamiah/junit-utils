package lu.mms.common.quality.junit.assets.fixture;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test class fixture file.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public @interface Fixture {

    /**
     * Inject test class (test case) mocks to the class with {@link Fixture} or not.
     * @return <b>true</b>, if the test class mocks should be injected.
     *         <b>false</b>, otherwise.
    */
    boolean injectMocks() default true;
}
