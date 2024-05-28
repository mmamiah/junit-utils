package lu.mms.common.quality.junit.assets.bdd.source;

import lu.mms.common.quality.assets.no.NoEnum;
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
public @interface EnumArgument {

    /**
     * The test scenario arguments from the provided enum.
     * @return The enum class.
    */
    Class<? extends Enum<?>> value() default NoEnum.class;

    /**
     * The enum values (string) to include.
     * @return The enum value names.
     */
    String[] include() default {};

    /**
     * The enum values (string) to exclude.
     * @return The enum value names.
     */
    String[] exclude() default {};

}
