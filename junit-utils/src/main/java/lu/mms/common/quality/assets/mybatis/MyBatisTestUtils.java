package lu.mms.common.quality.assets.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

/**
 * Utility class for MyBatis test.
 */
public final class MyBatisTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisTestUtils.class);

    private MyBatisTestUtils() {
        // hidden constructor
    }

    /**
     * Apply the given mig-script to the DataSource.
     * @param dataSource    The DataSource
     * @param resource      The path to the mig-script to apply
     * @throws IllegalStateException    Exception thrown when mig-script failed
     */
    public static void runScript(final DataSource dataSource, final String resource) {
        if (org.springframework.util.StringUtils.isEmpty(resource)) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            final ScriptRunner runner = new ScriptRunner(connection);
            runner.setAutoCommit(true);
            runner.setStopOnError(true);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            applyScript(resource, runner);
        } catch (Exception ex) {
            final String msg = String.format("Failed to execute migration script [%s].", resource);
            throw new IllegalStateException(msg, ex);
        }
    }

    private static void applyScript(final String resource, final ScriptRunner runner) throws IOException {
        final Reader reader = Resources.getResourceAsReader(resource);
        LOGGER.debug("Script [{}] loaded.", resource);
        runner.runScript(reader);
        LOGGER.debug("Script [{}] executed.", resource);
    }

}
