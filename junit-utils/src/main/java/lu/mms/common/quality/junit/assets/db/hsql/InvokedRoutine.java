package lu.mms.common.quality.junit.assets.db.hsql;

import lu.mms.common.quality.assets.mybatis.MyBatisMapperExtension;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * !!! Still under construction
 * {@link InvokedRoutine} is an annotation that is used to register {@linkplain MyBatisMapperExtension extensions}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "x.x.x"
)
public @interface InvokedRoutine {

    /**
     * @return The SQL routine name.
     */
    String name() default StringUtils.EMPTY;

}
