package lu.mms.common.quality.junit.assets.db.re.script;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileUrlResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public interface MigrationScript extends SqlScript {

    Logger LOGGER = LoggerFactory.getLogger(MigrationScript.class);

    File TEST_RESOURCES_DIRECTORY = new File("src/test/resources");

    DateTimeFormatter NOW_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SS");

    DateTimeFormatter LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral(StringUtils.SPACE)
            .append(ISO_LOCAL_TIME)
            .toFormatter(Locale.getDefault(Locale.Category.FORMAT));

    String BANNER =  StringUtils.LF
            + "--------------------------------------------- ---------------------------------------\n"
            + "--- Description: JUnit Utils - DB Reverse Engineering %s\n"
            + "--- Created on : %s \n"
            + "--- Execution time : %s \n"
            + "--------------------------------------------- ---------------------------------------\n";

    static String prepareBanner(final String executionTime) {
        final Object appVersion = Optional.ofNullable(MigrationScript.class.getResource("/pom.yml"))
                // collect the properties
                .map(pom -> {
                    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
                    factory.setResources(new FileUrlResource(pom));
                    return factory.getObject();
                })
                // retrieve the app version
                .map(properties -> ((Map<?,?>) properties.get("application")).get("version"));
        return String.format(BANNER, appVersion, LOCAL_DATE_TIME.format(LocalDateTime.now()), executionTime);
    }

    /**
     * Create the .sql file in the sql folder (test/resources).
     * @param packageProvider   The class been with in the target package
     * @param fileName  the files name
     * @param lines The file lines.
     */
    static boolean createFile(final Class<?> packageProvider, final String fileName, final List<String> lines) {
        Path file;
        if (packageProvider != null) {
            file = Path.of(
                    TEST_RESOURCES_DIRECTORY.getPath(),
                    StringUtils.replace(packageProvider.getPackageName(), ".", File.separator)
            );
            if (!file.toFile().exists()) {
                try {
                    file = Files.createDirectories(file);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
            file = Path.of(file.toString(), fileName);
        } else {
            file = Path.of(TEST_RESOURCES_DIRECTORY.getPath(), "sql", fileName);
        }

        boolean isCreated = false;
        try {
            isCreated = Files.write(file, lines, StandardCharsets.UTF_8).toFile().exists();
            LOGGER.info("File created: [{}]", file);
        } catch(IOException ex) {
            LOGGER.error("Failed to create the file [{}]", file);
        }
        return isCreated;
    }

    boolean createFile();

}
