package lu.mms.common.quality.assets.condition;

import org.apiguardian.api.API;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disable the test method on any other test failure..
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisableOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public @interface DisableOnFailure {

}
