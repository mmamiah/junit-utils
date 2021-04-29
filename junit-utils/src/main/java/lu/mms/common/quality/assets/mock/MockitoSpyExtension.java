package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.injection.InjectionEnhancerTemplate;
import lu.mms.common.quality.assets.mockvalue.MockValueExtension;
import lu.mms.common.quality.assets.mockvalue.commons.MockValueContainerInjectionTemplate;
import lu.mms.common.quality.assets.unittest.UnitTest;
import org.apache.commons.lang3.BooleanUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension to initialize Mockito {@link Spy}. <br>
 * The initialization consist into injecting the declared mocks and spies into the target spy if the spy name match or
 * if the target class match.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public class MockitoSpyExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockitoSpyExtension.class);

    @Override
    public void beforeEach(final ExtensionContext context) {
        final UnitTest unitTest = context.getRequiredTestClass().getDeclaredAnnotation(UnitTest.class);
        if (unitTest != null && !unitTest.initSpies()) {
            return;
        }

        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, context);
        // Applying the injection enhancer template with the 'Spy' annotation
        mocksContext.visit(InjectionEnhancerTemplate.newTemplate(Spy.class));

        // Inject @Value if relevant
        final ExtensionContext.Store store = getStore(context);
        if (BooleanUtils.isTrue(store.get(MockValueExtension.class, Boolean.class))) {
            MockValueContainerInjectionTemplate.newTemplate(
                    context.getRequiredTestInstance(), context.getRequiredTestMethod().getName()
            ).accept(Spy.class);
        }
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }

}
