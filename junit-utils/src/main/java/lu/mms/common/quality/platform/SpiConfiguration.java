package lu.mms.common.quality.platform;

import lu.mms.common.quality.JunitUtilsPreconditionException;
import lu.mms.common.quality.assets.AssetFactory;
import lu.mms.common.quality.utils.ConfigurationPropertiesUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.platform.launcher.TestExecutionListener;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static lu.mms.common.quality.utils.ConfigurationPropertiesUtils.isFancyBanner;
import static lu.mms.common.quality.utils.ConfigurationPropertiesUtils.isLogReflections;
import static lu.mms.common.quality.utils.ConfigurationPropertiesUtils.showBanner;
import static lu.mms.common.quality.utils.FrameworkUtils.findAssignableCandidate;

/**
 * The framework auto configuration.
 */
public final class SpiConfiguration implements TestExecutionListener {

    /**
     * The base assets package constant.
     */
    public static final String ROOT_PACKAGE = "lu.mms.common.quality.assets";

    private static final String JUNIT_PACKAGE = "org.junit";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpiConfiguration.class);

    /* Static Initialization*/
    static {
        try {
            LOGGER.debug("JUnit runner checkup ...");
            // Ensure JUnit is running
            junitValidation();

            LOGGER.debug("Applying configuration ...");

            // prepare the context (.properties files ...)
            initContext();

            // display banner file (with actual library version) if needed
            printBanner();

            // Disable Reflection logs if needed
            // TODO to be removed if not possible to disable logger.
            final boolean logReflections = isLogReflections();
            if (!logReflections && Reflections.log instanceof LocationAwareLogger) {
                final Logger logger = LoggerFactory.getLogger(Reflections.class);
                // FIXME to uncomment when switching to [slf4j v2+] with [logback v1.4.5+] (Spring Boot 3 ?)
                // logger.atLevel(org.slf4j.event.Level.ERROR);
            }

            // Factories initialisation
            final Map<Class<Annotation>, AssetFactory<Annotation>> factories = retrieveFactories();
            factories.values().stream()
                .sorted((one, two) -> ObjectUtils.compare(one.getOrder(), two.getOrder(), true))
                .forEach(AssetFactory::apply);
            LOGGER.debug("Configuration completed.");
        } catch (JunitUtilsPreconditionException ex) {
            LOGGER.error("An error occurred while running the library: [{}].", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Configuration failed: [{}].", ex.getMessage(), ex);
        }
    }

    /**
     * Framework SPI entry point.
     * @param args The app args
     */
    public static void main(final String[] args) {
        // Service Provider Interface entry point.
        // Do not implement any thing as all the initialisation is done when this class is loaded in the classpath
        // See the static block initializer above.
        LOGGER.debug("Framework started");
    }

    private static void junitValidation() {
        boolean isJUnit = false;
        for (StackTraceElement element: Thread.currentThread().getStackTrace()) {
            isJUnit = element.getClassName().startsWith(JUNIT_PACKAGE);
            if (isJUnit) {
                break;
            }
        }
        if (!isJUnit) {
            throw new JunitUtilsPreconditionException("JUnit not detected.");
        }
    }

    private static void initContext() {
        // init JUnit Utils Properties
        ConfigurationPropertiesUtils.initJunitUtilsProperties();
    }

    private static void printBanner() {
        final boolean isShowBanner = showBanner();
        if (!isShowBanner) {
            return;
        }

        final String bannerFilename;
        if (isFancyBanner()) {
            bannerFilename = "banner_fancy.txt";
        } else {
            bannerFilename = "banner.txt";
        }
        final URL bannerUrl = Thread.currentThread().getContextClassLoader().getResource(bannerFilename);
        if (bannerUrl == null) {
            return;
        }
        getBannerLogger().info(getBannerContent(bannerUrl));
    }

    /**
     * Retrieve a Map(Annotation, Factory) of annotations factories.
     * @param <A> The Annotation type
     * @param <F> The factory type
     * @return The factories Map.
     */
    static <A extends Annotation, F extends AssetFactory<A>> Map<Class<A>, F> retrieveFactories() {

        final Set<Class<? extends AssetFactory>> factoryClasses = findAssignableCandidate(AssetFactory.class);

        return factoryClasses.stream()
            // instantiate the factories
            .map(SpiConfiguration::<A, F>newFactoryInstance)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(AssetFactory::getType, factory -> factory));
    }

    private static <A extends Annotation, T extends AssetFactory<A>> T newFactoryInstance(
                                                            final Class<? extends AssetFactory> factoryClass) {
        T factoryInstance = null;
        try {
            factoryInstance = (T) ReflectionUtils.accessibleConstructor(factoryClass).newInstance((Object[]) null);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException ex) {
            LOGGER.error("Failed to instantiate the class.", ex);
        }
        return factoryInstance;
    }

    private static String getBannerContent(final URL bannerUrl) {
        String bannerFileContent = null;
        try {
            bannerFileContent = Files.readString(new File(bannerUrl.getPath()).toPath());
        } catch (IOException ex) {
            LOGGER.error("Failed to read the banner: [{}]", ex.getMessage(), ex);
        }
        return bannerFileContent;
    }

    private static java.util.logging.Logger getBannerLogger() {
        final java.util.logging.Logger bannerLogger = java.util.logging.Logger.getLogger("SPI_CUSTOM_LOGGER");
        bannerLogger.setUseParentHandlers(false);
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new BannerConsoleFormatter());
        bannerLogger.addHandler(consoleHandler);
        bannerLogger.setLevel(Level.INFO);
        return bannerLogger;
    }


}
