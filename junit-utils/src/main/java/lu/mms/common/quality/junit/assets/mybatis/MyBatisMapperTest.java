package lu.mms.common.quality.junit.assets.mybatis;

import lu.mms.common.quality.assets.db.InMemoryDb;
import lu.mms.common.quality.assets.no.NoClass;
import org.apache.commons.lang3.StringUtils;
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
    since = "1.0.0"
)
public @interface MyBatisMapperTest {

    /**
     * The InMemory database engine to be used to instantiate the datasource (session factory).
     * @return  The database engine.
     */
    InMemoryDb dbEngine() default InMemoryDb.HSQL_ORACLE;

    /**
     * The migration scripts to run when configuring the DataSource. <br>
     * The script will be executed in the declared order.
     * @return The script.
     */
    String[] script() default StringUtils.EMPTY;

    /**
     * Manage the connection to the database.
     * @return <b>true</b>, if <i>@Test</i> methods should be isolated from the other test methods.
     *                      Then, the connection will be closed after each method.<br>
     *         <b>false</b>, otherwise.
     */
    boolean testIsolation() default true;

    Class<?>[] sqlRoutines() default NoClass.class;

}
