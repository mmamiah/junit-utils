package lu.mms.common.quality.junit.assets.bdd.source;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The tests scenario arguments provider.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public @interface ValueArgument {

    /**
     * The test scenario arguments (String).
     * @return The string values.
     */
    String[] value() default {};

}
