package lu.mms.common.quality.junit.assets.testutils;

import lu.mms.common.quality.assets.fixture.FixtureExtension;
import lu.mms.common.quality.assets.mock.MockInjectionExtension;
import lu.mms.common.quality.assets.mock.MockitoSpyExtension;
import lu.mms.common.quality.assets.mock.ReturnsMocksExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.context.MocksContextParameterResolver;
import lu.mms.common.quality.assets.mock.reinforcement.ReinforceMockExtension;
import lu.mms.common.quality.assets.mockvalue.MockValue;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link ExtendWithTestUtils} annotation, which extends the annotated test class with framework relevant
 * functionalities. <br>
 * As per JUnit 5 Extension model, the extension order is deterministic.
 * <ol>
 *     <li>{@link MockitoExtension}: Initializing mockito (settings, @Mock, @Spy, @InjectMocks ...).</li>
 *
 *     <li>
 *         {@link ReturnsMocksExtension}: Customise {@link Answers} when relevant.<br>
 *         <b>[Goal #1]</b> Ensure that the related mocks will return the test instance declared @Mock/@Spy.
 *     </li>
 *     <li>
 *         {@link MockValueExtension}: Initialize the <b>@Value</b> when relevant (via {@link MockValue}).<br>
 *         <b>[Goal #2]</b> Ensure that the @Value field are initialized in the structure resulting from (#1 and #2).
 *     </li>
 *     <li>
 *         {@link ReinforceMockExtension}: Enrich the declared mocks to avoid NPE on lifecycle methods call.<br>
 *         <b>[Goal #3]</b> Ensure that the @Mock/@Spy will be fulfilled with declared mocks/spies.
 *     </li>
 *     <li>
 *         {@link MockitoSpyExtension}: Apply the test instance mocks and value the spies.<br>
 *         <b>[Goal #4]</b> Enrich the @Spy fields with the previous (#1, #2, #3) knowledge.
 *     </li>
 *     <li>
 *         {@link MockInjectionExtension}: Apply Constructor, Field/Setter and Lookup method injects to the
 *         SUT (@InjectMocks). Mocks collection/array is taken into account as well.<br>
 *         <b>[Goal #5]</b> Ensure that  the SUT (@InjectMocks) is initialized respectively.
 *     </li>
 *
 *     <li>{@link FixtureExtension}: Initialize the fixture files.</li>
 *     <li>{@link MocksContextParameterResolver}: Parameter resolver for {@link InternalMocksContext}.</li>
 * </ol>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
// IMPORTANT: Extension list order is important. They are executed sequentially.
@ExtendWith({
    // Mandatory framework extension should on top of the stack
    MockitoExtension.class,

    // Functional extensions
    ReturnsMocksExtension.class,    // [#1] ensure the created mock will return the test instance declared @Mock/@Spy
    MockValueExtension.class,       // [#2] initialize the @Value (via @MockValue) when relevant
    ReinforceMockExtension.class,   // [#3] enrich the declared mocks to avoid NPE on lifecycle methods call
    MockitoSpyExtension.class,      // [#4] enrich the @Spy fields with the previous (#1 and #2) knowledge
    MockInjectionExtension.class,   // [#5] ensure the SUT (@InjectMocks) is properly initialized ...

    // Helper extensions
    FixtureExtension.class,

    // Parameter Resolvers
    MocksContextParameterResolver.class
})
@Documented
@API(
    status = API.Status.STABLE,
    since = "1.0.0"
)
public @interface ExtendWithTestUtils {

    /**
     * The value to be set as {@link Tag}.
     * @return The Tag values.
     */
    String[] tags() default StringUtils.EMPTY;

    /**
     * Answer with mocks defined in user test (test instance mocks context). <br>
     * Add or not a custom behavior when mock declared with @Mock(answer = Answers.RETURNS_MOCKS) <br>
     * <b>see: </b> {@link Answers} <br>
     * @return <b>true</b>,  Return mocks defined in test instance. <br>
     *         <b>false</b>,  Mockito Answers default behavior (a new mock will be returned.).
     */
    boolean returnMocks() default true;

    /**
     * Enable SUT constructor injection, field/setter inject and Lookup method injection. <br>
     * @see MockInjectionExtension
     * @return <b>true</b>,  enable enhanced mock injection<br>
     *         <b>false</b>, disable enhanced mock injection
     */
    boolean initMocks() default true;

    /**
     * Enable declared {@link Spy} constructor injection, field/setter injection and Lookup method injection.
     * @see MockitoSpyExtension
     * @return <b>true</b>,  enable enhanced Spies injection <br>
     *         <b>false</b>, disable enhanced Spies injection
     */
    boolean initSpies() default true;

    /**
     * Ensure that each test instance mock is reinforced, by injected declared mock to it if relevant..
     * @see ReinforceMockExtension
     * @return <b>true</b>,  enable mock reinforcement <br>
     *         <b>false</b>, disable mock reinforcement
     */
    boolean reinforceMock() default true;
}
