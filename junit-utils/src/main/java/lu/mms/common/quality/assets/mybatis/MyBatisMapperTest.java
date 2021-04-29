package lu.mms.common.quality.assets.mybatis;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MyBatisMapperTest annotation.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(MyBatisExtension.class)
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.0"
)
public @interface MyBatisMapperTest {

    /**
     * The SQL scripts to run when configuring the DataSource. <br>
     * The script will be executed in the declared order.
     * @return The script.
     */
    String[] script() default "";

    /**
     * Manage the connection to the database.
     * @return <b>true</b>, if <i>@Test</i> methods should be isolated from the other test methods.
     *                      Then, the connection will be closed after each method.<br>
     *         <b>false</b>, otherwise.
     */
    boolean testIsolation() default true;

    /**
     * This parameter represent an array of Mapper classes to be added to MyBatis configuration.
     * @return The array of mapper classes to register.
     */
    Class<?>[] mapperClass();

}
