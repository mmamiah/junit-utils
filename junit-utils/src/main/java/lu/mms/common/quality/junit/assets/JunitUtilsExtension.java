package lu.mms.common.quality.junit.assets;

import lu.mms.common.quality.junit.assets.mock.context.InternalMocksContext;
import lu.mms.common.quality.junit.platform.SpiConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

/**
 * This class provides the basic functionality for any JUnit Utils Extension.
 */
public abstract class JunitUtilsExtension implements Consumer<InternalMocksContext>, BeforeAllCallback,
                                                    AfterAllCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(JunitUtilsExtension.class);
    private static final String MIN_SPRING_BOOT_TEST_VERSION = "2.6.0";

    // Static block to ensure that the SPI class is loaded
    static {
        try {
            Class.forName(SpiConfiguration.class.getName());
        } catch (final Throwable throwable) {
            LOGGER.warn("Failed to load the Service Provider API");
        }
    }

    /**
     * The JUnit Utils namespace.
     */
    public static final ExtensionContext.Namespace JUNIT_UTILS_NAMESPACE = create("junit.utils");

    @Override
    public void beforeAll(final ExtensionContext extensionContext) {
        // spring version gatekeeper.
        assumeTrue(
                isValidVersion(SpringBootVersion.getVersion()),
                String.format(
                        "'Spring Boot Test' minimum required version is [%s]. You are using version [%s]",
                        MIN_SPRING_BOOT_TEST_VERSION, SpringBootVersion.getVersion()
                )
        );

        final ExtensionContext.Store store = getStore(extensionContext);
        store.put(getClass(), true);
    }

    static boolean isValidVersion(final String actual) {
        final int requiredVersion = Integer.parseInt(MIN_SPRING_BOOT_TEST_VERSION.replaceAll("\\.", StringUtils.EMPTY));
        final int actualVersion = Integer.parseInt(actual.replaceAll("\\.", StringUtils.EMPTY));
        return actualVersion >= requiredVersion;
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
