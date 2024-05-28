package lu.mms.common.quality.assets.fixture;

import lu.mms.common.quality.assets.JunitUtilsTestContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.ReflectionUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static lu.mms.common.quality.assets.JunitUtilsExtension.JUNIT_UTILS_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixtureExtensionTest {

    @Mock(lenient = true)
    private ExtensionContext extensionContextMock;

    @Spy
    private final ExtensionContext.Store storeSpy = new JunitUtilsTestContextStore();

    @Mock
    private BookService bookServiceMock;

    @Mock
    private CarService carServiceMock;

    @Fixture
    private FixtureOpenToInjection fixtureOpenToInjection;

    @Fixture(injectMocks = false)
    private FixtureOpenToInjection fixtureOpenToInjectionByDefinitionButClosedByFlag;

    @Fixture(injectMocks = false)
    private FixtureClosedToInjection fixtureCloseByDefinitionAndByFlag;

    @Fixture
    private FixtureClosedToInjection fixtureCloseByDefinition;

    @Fixture(injectMocks = false)
    private NotAnnotatedFixture notAnnotatedFixtureClosedByFlag;

    @BeforeEach
    void init(final TestInfo testInfo) {
        when(extensionContextMock.getStore(JUNIT_UTILS_NAMESPACE)).thenReturn(storeSpy);

        when(extensionContextMock.getRequiredTestInstance()).thenReturn(this);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) getClass());
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(testInfo.getTestMethod().orElse(null));
    }

    @TestFactory
    List<DynamicContainer> shouldNotInitFixtureWhenInTheTestcaseNotTheSameAsInTheFixtureFile() {
        return Arrays.asList(
            dynamicContainer("With Dependency Injection", getDynamicTestsWithDependencyInjection(true)),
            dynamicContainer("Without Dependency Injection", getDynamicTestsWithDependencyInjection(false))
        );
    }

    private List<DynamicTest> getDynamicTestsWithDependencyInjection(final boolean withDependencyInjection) {
        return ReflectionUtils
            .getAllFields(getClass(), ReflectionUtils.withAnnotation(Fixture.class))
            .parallelStream()
            // keep the field open to inject only
            .filter(field -> withDependencyInjection == isOpenToDependencyInjection(field))
            // keep the class open to inject only
            .filter(field -> withDependencyInjection == isOpenToDependencyInjection(field.getType()))
            .map(field -> dynamicTest(field.getName(), () -> testTemplate(field.getName(), withDependencyInjection)))
            .collect(Collectors.toList());
    }

    /**
     * This is the generic test method.
     * @param fixtureFieldName The fixture field to test
     * @param withDependencyInjection with dependency injection flag
     */
    private void testTemplate(final String fixtureFieldName, final boolean withDependencyInjection) {
        // Arrange
        new FixtureExtension().beforeEach(extensionContextMock);

        // get the field after it has been initialized
        final Object fixture = ReflectionTestUtils.getField(this, fixtureFieldName);
        assumeTrue(fixture != null);

        // find the method which name have the "given" prefix
        ReflectionUtils.getAllMethods(fixture.getClass(), method -> method.getName().startsWith("given"))
            .forEach(method -> {
                if (withDependencyInjection && fixture instanceof FixtureOpenToInjection) {
                    assertValueInitialized((FixtureOpenToInjection) fixture, method);
                } else {
                    assertValueNotInitialized(fixture, method);
                }
            });
    }

    private void assertValueInitialized(final FixtureOpenToInjection fixture, final Method method) {
        // Act
        ReflectionTestUtils.invokeMethod(fixture, method.getName());

        // Assert
        assertThat(bookServiceMock.getSize(), equalTo(20));
        assertThat(fixture.getBookService().getSize(), equalTo(20));

        // confirm CarService has been overrided as well
        assertThat(fixture.getCarService(), notNullValue());
        assertThat(fixture.getCarService(), equalTo(carServiceMock));
    }

    private void assertValueNotInitialized(final Object fixture, final Method method) {
        // Act
        Exception error = null;
        try {
            ReflectionTestUtils.invokeMethod(fixture, method.getName());
        } catch (Exception ex) {
            error = ex;
        }

        // Assert
        assertThat(error, instanceOf(NullPointerException.class));
    }

    private static boolean isOpenToDependencyInjection(final AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Fixture.class)) {
            return annotatedElement.getAnnotation(Fixture.class).injectMocks();
        }
        return true;
    }

}
