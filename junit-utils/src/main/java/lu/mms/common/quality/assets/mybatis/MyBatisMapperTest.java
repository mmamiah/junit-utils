package lu.mms.common.quality.assets.mybatis;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link MyBatisMapperTest} is an annotation that is used to register {@linkplain MyBatisMapperExtension extensions}
 * which will setup the minimal MyBatis environment, initialize the datasource and register the declared
 * {@linkplain InjectMapper mapper fields}, to make then accessible for the test cases.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(MyBatisMapperExtension.class)
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public @interface MyBatisMapperTest {

    /**
     * The migration scripts to run when configuring the DataSource. <br>
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

}
