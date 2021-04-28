package lu.mms.common.quality.assets.fixture;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.internal.configuration.DefaultInjectionEngine;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fixture extension.<br>
 * This extension will do the following:
 * <ol>
 *     <li>
 *         scan the test class for field annotated with {@link Fixture} and instantiate it as per the field type.
 *     </li>
 *     <li>
 *         after instantiation is done, in case {@link Fixture#injectMocks()} == <b>true</b>, then if a
 *         mock was declared in the test class and is also declared in the fixture file, that mock will be injected
 *         in the fixture file.
 *     </li>
 * </ol>
 * the file path and find any (if needed) class annotated with {@link Fixture},
 * and inject
 */
public class FixtureExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixtureExtension.class);

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        final Object testInstance = extensionContext.getRequiredTestInstance();
        final Set<Field> mockDependentFields = ReflectionUtils.getAllFields(
            extensionContext.getRequiredTestClass(),
            ReflectionUtils.withAnnotation(Fixture.class)
        )
        .stream()
        // instantiate fixture field
        .peek(foundField -> instantiateFixture(testInstance, foundField))
        .filter(FixtureExtension::isOpenToDependencyInjection)
        .collect(Collectors.toSet());

        // inject mocks into target Fixture fields
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, extensionContext);
        (new DefaultInjectionEngine()).injectMocksOnFields(
                mockDependentFields,
                mocksContext.getMocks(),
                testInstance
        );
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }

    private void instantiateFixture(final Object testInstance, final Field foundField) {
        final Class<?> fixtureClass = foundField.getType();
        // instantiate the fixture (@Fixture) test class field
        try {
            final Object fixtureInstance = fixtureClass.getDeclaredConstructor().newInstance((Object[]) null);
            ReflectionTestUtils.setField(testInstance, foundField.getName(), fixtureInstance);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException
                | IllegalAccessException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private static boolean isOpenToDependencyInjection(final Field field) {
        // check the class annotation
        boolean isOpen = true;
        if (field.getType().isAnnotationPresent(Fixture.class)) {
            isOpen = field.getType().getAnnotation(Fixture.class).injectMocks();
        }
        // check the field annotation
        if (isOpen && field.isAnnotationPresent(Fixture.class)) {
            isOpen = field.getAnnotation(Fixture.class).injectMocks();
        }
        return isOpen;
    }

}
