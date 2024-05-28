package lu.mms.common.quality.assets.mock.reinforcement;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.assets.mock.injection.FieldAndSetterInjection;
import lu.mms.common.quality.assets.testutils.ExtendWithTestUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This extension will inject known mocks (from the test instance, via the {@link InternalMocksContext})
 * in the @Mock fields. The goal is to avoid any NPE during Lifecycle method execution. Such situation happens when
 * injecting the mocks in the (spring) application context.
 */
@API(
        status = API.Status.EXPERIMENTAL,
        since = "1.0.0"
)
public class ReinforceMockExtension extends JunitUtilsExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReinforceMockExtension.class);

    private static final FieldAndSetterInjection FIELD_INJECTION = FieldAndSetterInjection.newConsumer(Mock.class);

    @Override
    public void beforeAll(final ExtensionContext extensionContext) {
        super.beforeAll(extensionContext);

        final ExtensionContext.Store store = getStore(extensionContext);
        store.put(MockReinforcementHandler.class, FIELD_INJECTION);
    }

    @Override
    public void afterAll(final ExtensionContext extensionContext) {
        super.afterAll(extensionContext);

        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(MockReinforcementHandler.class, FieldAndSetterInjection.class);
    }

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        final ExtendWithTestUtils config = extensionContext.getRequiredTestClass()
                .getDeclaredAnnotation(ExtendWithTestUtils.class);
        if (config != null && !config.reinforceMock()) {
            return;
        }

        final InternalMocksContext mocksContext = retrieveMocksContext(LOGGER, extensionContext);
        mocksContext.visit(FIELD_INJECTION);

    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(extensionContext.getRequiredTestMethod().getName(), InternalMocksContext.class);
    }
}
