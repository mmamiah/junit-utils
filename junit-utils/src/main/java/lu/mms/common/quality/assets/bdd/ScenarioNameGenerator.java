package lu.mms.common.quality.assets.bdd;

import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class turn the test class name as well as the test method names into human readable names.<br>
 * In case the test class or the test method is annotated with @{@link DisplayName}, then no action is taken and the
 * {@link DisplayName#value} is returned.<br>
 * e.g:
 * <ol>
 *     <li>{@code "givenAUserWithAnID" -> "Given a user with an ID"}</li>
 *     <li>{@code "whenICheckHisAccountNumber" -> "When I check his account number"}</li>
 *     <li>{@code "thenTheAccountIsValid" -> "Then the account is valid"}</li>
 * </ol>
 */
@API(
        status = API.Status.EXPERIMENTAL,
        since = "1.0.0"
)
public class ScenarioNameGenerator extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForClass(final Class<?> testClass) {
        final DisplayName displayName = testClass.getAnnotation(DisplayName.class);
        if (displayName != null) {
            return displayName.value();
        }
        return formatScenarioDisplayName(StringUtils.splitByCharacterTypeCamelCase(testClass.getSimpleName()));
    }

    @Override
    public String generateDisplayNameForMethod(final Class<?> testClass, final Method testMethod) {
        final DisplayName displayName = testMethod.getAnnotation(DisplayName.class);
        if (displayName != null) {
            return displayName.value();
        }
        final String methodName = Stream.of(testMethod.getName().split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
        return formatDisplayName(StringUtils.splitByCharacterTypeCamelCase(methodName));
    }

    @Override
    public String generateDisplayNameForNestedClass(final Class<?> nestedClass) {
        return generateDisplayNameForClass(nestedClass);
    }

    private String formatScenarioDisplayName(final String[] words) {
        return formatDisplayName(words);
    }

    private String formatDisplayName(final String[] words) {
        final StringBuilder displayName = new StringBuilder();
        IntStream.range(0, words.length).forEach(index -> {
            boolean nextWordIsTest = (index < (words.length - 1)) && words[index + 1].equalsIgnoreCase("test");
            if (index == 0) {
                displayName.append(StringUtils.capitalize(words[index]));
            } else if (words[index].length() == 1 && nextWordIsTest) {
                // Turn name related to tests like: ITest -> 'I. test', 'CTest' -> 'C. test'
                displayName.append(words[index]).append(".");
            } else {
                displayName.append(words[index].toLowerCase());
            }
            displayName.append(" ");
        });
        return displayName.toString().trim();
    }

}
