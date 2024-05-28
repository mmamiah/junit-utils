package lu.mms.common.quality.junit.assets.mock.context;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This extension make the {@link InternalMocksContext} available for the test method/case.
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "1.0.0"
)
public class MocksContextParameterResolver extends JunitUtilsExtension implements ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MocksContextParameterResolver.class);

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == InternalMocksContext.class;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return retrieveMocksContext(LOGGER, extensionContext);
    }

}
