package lu.mms.common.quality.assets.unittest;

import lu.mms.common.quality.assets.JunitUtilsExtension;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Extension for {@link UnitTest} annotation. <br>
 * The main purpose of this extension is to apply common logic to target class, such as:<br>
 * - 'MockitoAnnotations.openMocks(...)' if UnitTest.initMocks() is 'true'
 */
@API(
    status = API.Status.EXPERIMENTAL,
    since = "0.0.0"
)
public class UnitTestExtension extends JunitUtilsExtension
                                implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestExtension.class);

    @Override
    public void beforeTestExecution(final ExtensionContext context) {
        LOGGER.info("+- Starting [{}]", context.getRequiredTestMethod().getName());
    }

    @Override
    public void afterTestExecution(final ExtensionContext context) {
        LOGGER.info("+- Executed [{}]", context.getRequiredTestMethod().getName());
    }

}
