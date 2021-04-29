package lu.mms.common.quality.assets.mybatis;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject the MyBatis mapper of the annotated field type.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.0"
)
public @interface InjectMapper {
}
