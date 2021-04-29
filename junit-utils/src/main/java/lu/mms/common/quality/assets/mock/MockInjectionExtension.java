package lu.mms.common.quality.assets.mock;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.injection.InjectionEnhancerTemplate;
import lu.mms.common.quality.assets.unittest.UnitTest;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This extension helps to collect all the mocks into the appropriate collection or array, and inject it in the target
 * instance if relevant. <br>
 * In case this extension is used together with the {@link UnitTest} annotation, the injection will take place only if
 * the property UnitTest.initMocks() is 'true'.
 *
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.1"
)
public class MockInjectionExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockInjectionExtension.class);

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        final UnitTest unitTest = extensionContext.getRequiredTestClass().getDeclaredAnnotation(UnitTest.class);
        if (unitTest != null && !unitTest.initMocks()) {
            return;
        }
        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, extensionContext);

        // Applying the injection enhancer template
        mocksContext.visit(InjectionEnhancerTemplate.newTemplate(InjectMocks.class));
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }

}
