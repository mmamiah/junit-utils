package lu.mms.common.quality.junit.assets.bdd;

import lu.mms.common.quality.assets.bdd.source.EnumArgument;
import lu.mms.common.quality.assets.bdd.source.ValueArgument;
import lu.mms.common.quality.assets.no.NoEnum;
import lu.mms.common.quality.utils.ExceptionMessageUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * This abstract class is to be used for BDD testing.
 */
public abstract class ScenarioTesting {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioTesting.class);

    private static final Set<Class<? extends Annotation>> ACCEPTED_ANNOTATIONS = Set.of(DisplayName.class);
    private static final ScenarioNameGenerator NAME_GENERATOR = new ScenarioNameGenerator();
    private static final String SCENARIO = "Scenario #%s : %s";
    private static final String INVALID_NUMBER_OF_ARGUMENTS =
            "Invalid number of arguments for method [%s]. Excepted (%s), but was (%s)";
    private static final String INVALID_ARGUMENTS_TYPE =
            "Invalid arguments type for method [%s]. Excepted (%s), but was (%s)";
    private final AtomicInteger countScenarios = new AtomicInteger(1);
    private List<Method> testFactoriesMethods;

    @BeforeEach
    void collectTestFactoryMethodNames() {
        testFactoriesMethods = Stream.of(ReflectionUtils.getDeclaredMethods(this.getClass()))
                .filter(ScenarioTesting::isValidMethod)
                .collect(Collectors.toList());
    }

    /**
     * Collect the test class declared method (method without annotation or with accepted annotation) and map then
     * into a list of dynamic tests.
     * @return List of dynamic tests
     */
    protected List<DynamicNode> getScenarioDynamicTest() {
        return getScenarioDynamicTest(null);
    }


    /**
     * Collect the test class declared method (method without annotation or with accepted annotation) and map then
     * into a list of dynamic tests.
     * @param arguments The arguments to use when building the scenario dynamic tests
     * @return List of dynamic tests
     */
    protected List<DynamicNode> getScenarioDynamicTest(final Object[] arguments) {
        final AtomicReference<FailureDetails> failureDetails = new AtomicReference<>();
        return testFactoriesMethods.stream()
                .sorted(MethodNameComparatorUtils.getDisplayNameComparator())
                .map(method -> DynamicTest.dynamicTest(
                        NAME_GENERATOR.generateDisplayNameForMethod(this.getClass(), method),
                        () -> {
                            boolean anErrorOccurred = failureDetails.get() != null;
                            if (anErrorOccurred) {
                                failureDetails.set(new FailureDetails("one of the previous test failed", method.getName()));
                            }
                            // check if test method should be executed
                            assumeFalse(anErrorOccurred, ObjectUtils.defaultIfNull(failureDetails.get(), StringUtils.EMPTY).toString());

                            // execute the test method
                            try {
                                final Object[] args = retrieveTestMethodArguments(arguments, method);
                                ReflectionTestUtils.invokeMethod(this, method.getName(), args);
                            } catch (Throwable ex) {
                                final String msg = String.format(
                                        "Failed to execute test method '%s'. Cause: '%s'",
                                        method.getName(), ExceptionMessageUtils.extractErrorMessage(ex)
                                );
                                failureDetails.set(new FailureDetails(msg, method.getName()));
                                throw ex;
                            }
                        }
                    )
                )
                .collect(Collectors.toList());
    }

    /**
     * This method build dynamic tests and should be called at the target test class, under the method
     * annotated with {@link TestFactory}, return a Stream of {@link DynamicNode}. <br>
     * <br>
     * <b>Example of BDD tests:</b>
     * <pre>
     *  class ScenarioDrivenTest extends ScenarioTesting {
     *     {@code @}{@link TestFactory}
     *      public {@link Stream}{@code < }{@link DynamicNode}{@code >} myScenariosTestFactory() {
     *          return super.scenariosTestFactoryTemplate();
     *      }
     *
     *      void givenNewCustomer() {
     *          // Given
     *      }
     *
     *      void whenCustomerAppliesForNewAccount() {
     *          // When
     *      }
     *
     *      void thenAccountIsCreated() {
     *          // Then
     *      }
     *  }
     * </pre>
     * <br>extends Object
     * The methods within the test class (except the one annotated with {@link TestFactory}) will be executed in a
     * 'GIVEN-WHEN-THEN' order, no matter in which order they were declared.
     * @return A Stream of {@link DynamicNode}s.
     */
    protected Stream<DynamicNode> scenariosTestFactoryTemplate() {
        final EnumArgument enumArgument = this.getClass().getAnnotation(EnumArgument.class);
        final ValueArgument valueArgument = this.getClass().getAnnotation(ValueArgument.class);

        boolean isActiveArguments = false;
        Stream<DynamicNode> enumNodes = Stream.empty();
        if (enumArgument != null && enumArgument.value() != NoEnum.class) {
            isActiveArguments = true;
            final Class<? extends Enum<?>> enumClass = enumArgument.value();
            enumNodes = Stream.of(enumClass.getEnumConstants())
                    // Keep the relevant enums as per 'include' subset
                    .filter(enumConst ->
                            ArrayUtils.isEmpty(enumArgument.include())
                                    || ArrayUtils.contains(enumArgument.include(), enumConst.name())
                    )
                    // Keep the relevant enums as per 'exclude' subset
                    .filter(enumConst ->
                            ArrayUtils.isEmpty(enumArgument.exclude())
                                    || !ArrayUtils.contains(enumArgument.exclude(), enumConst.name())
                    )
                    .map(enumConst -> DynamicContainer.dynamicContainer(
                            formatScenarioName(enumConst.name()),
                            getScenarioDynamicTest(new Object[]{enumConst})
                            )
                    );
        }
        Stream<DynamicNode> stringNodes = Stream.empty();
        if (valueArgument != null && ArrayUtils.isNotEmpty(valueArgument.value())) {
            isActiveArguments = true;
            stringNodes = Stream.of(valueArgument.value())
                    .map(stringValue -> DynamicContainer.dynamicContainer(
                            formatScenarioName(stringValue),
                            getScenarioDynamicTest(ArrayUtils.toArray(stringValue))
                            )
                    );
        }

        return isActiveArguments ? Stream.concat(enumNodes, stringNodes) : getScenarioDynamicTest().stream();
    }

    /**
     * Format the scenario name, to be displayed in the test plan.
     * @param scenarioName The scenario name
     * @return  The formatted scenario name
     */
    public String formatScenarioName(final String scenarioName) {
        return String.format(SCENARIO, countScenarios.getAndIncrement(), scenarioName);
    }

    private static boolean isValidMethod(final Method method) {
        if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
            LOGGER.warn("Skipping '{}' method because it is STATIC or PRIVATE.", method.getName());
            return false;
        }
        final Annotation[] annotations = method.getDeclaredAnnotations();
        return ArrayUtils.isEmpty(annotations) || Stream.of(annotations)
                .map(Annotation::annotationType)
                .allMatch(ACCEPTED_ANNOTATIONS::contains);
    }

    private Object[] retrieveTestMethodArguments(final Object[] arguments, final Method method) {
        final int expectedLength = method.getParameterCount();
        final int actualLength = ArrayUtils.getLength(arguments);
        if (expectedLength > 0 && expectedLength != actualLength) {
            throw new IllegalArgumentException(
                    String.format(INVALID_NUMBER_OF_ARGUMENTS, method.getName(), expectedLength, actualLength)
            );
        }
        return IntStream.range(0, expectedLength)
                .mapToObj(index -> {
                    final Class<?> argType = arguments[index].getClass();
                    final Class<?> expectedType = method.getParameterTypes()[index];
                    if (expectedType.isAssignableFrom(argType)) {
                        return arguments[index];
                    }
                    LOGGER.error(String.format(
                            "Not allowed to invoke method [%s] with argument(s) [%s] of type [%s]",
                            method.getName(), arguments[index], argType
                    ));
                    throw new IllegalArgumentException(
                            String.format(INVALID_ARGUMENTS_TYPE, method.getName(), expectedType, argType)
                    );
                })
                .toArray();
    }

}
