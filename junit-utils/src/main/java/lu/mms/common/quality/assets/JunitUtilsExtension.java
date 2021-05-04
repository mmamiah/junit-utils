package lu.mms.common.quality.assets;

import lu.mms.common.quality.assets.mock.context.InternalMocksContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;

import java.util.function.Consumer;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

/**
 * This class provide the basic functionality for any JUnit Utils Extension.
 */
public abstract class JunitUtilsExtension implements Consumer<InternalMocksContext>, BeforeAllCallback,
                                                    AfterAllCallback {

    /**
     * The JUnit Utils namespace.
     */
    public static final ExtensionContext.Namespace JUNIT_UTILS_NAMESPACE = create("junit.utils");

    @Override
    public void beforeAll(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.put(getClass(), true);
    }

    @Override
    public void afterAll(final ExtensionContext extensionContext) {
        final ExtensionContext.Store store = getStore(extensionContext);
        store.remove(getClass());
    }

    @Override
    public void accept(final InternalMocksContext mocksContext) {
        // default Mocks Context visitor
    }

    /**
     * This method retrieve the {@link InternalMocksContext} from a given {@link ExtensionContext}.
     * If the mocks context wasn't yet defined, then a new one is instantiated and added to the extension context.
     * @param logger The parent logger
     * @param extensionContext The Extension context
     * @return The mocks context object.
     */
    public InternalMocksContext retrieveMocksContext(final Logger logger, final ExtensionContext extensionContext) {
        final String testMethodName = extensionContext.getRequiredTestMethod().getName();

        final ExtensionContext.Store store = getStore(extensionContext);

        final InternalMocksContext mocksContext = store.getOrComputeIfAbsent(
                testMethodName,
                methodName ->
                    InternalMocksContext.newContext(
                            logger,
                            extensionContext.getRequiredTestClass(),
                            extensionContext.getRequiredTestInstance(),
                            methodName
                    ),
                InternalMocksContext.class);

        store.put(testMethodName, mocksContext);

        return mocksContext;
    }

    protected static ExtensionContext.Store getStore(final ExtensionContext context) {
        return context.getStore(JUNIT_UTILS_NAMESPACE);
    }

}
