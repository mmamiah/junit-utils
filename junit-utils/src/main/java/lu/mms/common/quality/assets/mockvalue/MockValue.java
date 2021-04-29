package lu.mms.common.quality.assets.mockvalue;


import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>@MockValue</b> annotation will initialise the subject under test (<b>@InjectMock</b> annotated) properties. <br>
 * The target properties are the ones annotated with the {@link Value}, where <b>'@MockValue#value()'</b> array
 * contains <b>'@Value#value()'</b>.<br>
 * <br><br>
 * i.e:<br>
 * <blockquote><pre>
 * &#64;ExtendWith({MockValueExtension.class})
 * class MyTest {
 *
 *    &#64;InjectMocks
 *    private Customer sut;
 *
 *    &#64;MockValue(value = {"customer.name", "customer.address"})
 *    private String customerNameMock = "alpha";
 *
 *    &#64;Test
 *    void shouldVerifyThatSutHaveInitializedValue(){
 *      assert sut.getName().equals("alpha") // --------- <b>This assert will be valid</b>
 *      assert sut.getAddress().equals("alpha") // --------- <b>This assert will be valid</b>
 *    }
 * }
 *
 * class Customer {
 *    &#64;Value("customer.name")
 *    private String name;
 *
 *    &#64;Value("customer.address")
 *    private String address;
 *
 *    String getName(){
 *      return name;
 *    }
 *
 *    String getAddress(){
 *      return address;
 *    }
 * }
 * </pre></blockquote>
 * <b>impl note:</b> {@link Value} using Spring Expression Language are not supported
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public @interface MockValue {

    /**
     * An array of values ({@link Value#value()}) to look for in the subject under test.
     * @return the value
     */
    String[] value();

    /**
     * The test method where to apply the ({@link Value#value()}).
     * @return the test cases
     */
    String[] testcase() default {};

}
