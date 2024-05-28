package lu.mms.common.quality.assets.bdd;

import lu.mms.common.quality.assets.conditions.DisableTestMethodOnFailureExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static lu.mms.common.quality.assets.bdd.MethodNameComparatorUtils.GIVEN;
import static lu.mms.common.quality.assets.bdd.MethodNameComparatorUtils.THEN;
import static lu.mms.common.quality.assets.bdd.MethodNameComparatorUtils.WHEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

@ExtendWith(DisableTestMethodOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisplayNameComparatorTest {

    private static final String GIVEN_CITIZEN_CAR = "given the citizen car";
    private static final String AND_GIVEN_SOMEONE_PETS = "and given another pet";
    private static final String GIVEN_SOMEONE_PETS = "given someone pets";
    private static final String GIVEN_A_FREE_FAMILY_PETS = "given a free family";
    private static final String WHEN_WE_CALCULATE_TAXES = "when we calculate taxes";
    private static final String WHEN_THE_CITIZEN_IS_WORKING_HARD = "when the citizen is working hard";
    private static final String AND_WHEN_THE_CITIZEN_START_B2B = "and when the citizen need to start own business";
    private static final String THEN_CITIZEN_KIDS_ARE_HAPPY = "then citizen kids are happy";
    private static final String AND_THEN_CITIZEN_PARENTS_ARE_HAPPY = "and then citizen parents are happy";
    private static final String THEN_CITIZEN_HOUSE_NOT_CHARGE = "then citizen house is not charged";

    @ParameterizedTest(name = "[{index}][{1}] Should sort names when simple comparison.")
    @MethodSource("argumentsSortingLevelOneProvider")
    @Order(0)
    public void shouldSortNamesWhenSingleLevelComparison(final List<String> entries, final String bddKey,
                                                  final List<String> results) {
        // Arrange
        final DisplayNameComparator sut = new DisplayNameComparator(bddKey);

        // Act
        entries.sort(sut);

        // Assert
        assertThat(entries, iterableWithSize(6));
        results.forEach(entry -> assertThat(entries.indexOf(entry), lessThanOrEqualTo(1)));
    }

    @ParameterizedTest(name = "[{index}][{1}] Should sort names when complex comparison.")
    @MethodSource("argumentsSortingLevelTwoProvider")
    @Order(2)
    public void shouldSortNamesWhenTwoLevelComparison(final List<String> entries, final String bddKeyOne,
                                               final String bddKeyTwo, final List<String> results) {
        // Arrange
        final Comparator<String> sut = new DisplayNameComparator(bddKeyOne)
                                        .thenComparing(new DisplayNameComparator(bddKeyTwo));

        // Act
        entries.sort(sut);

        // Assert
        assertThat(entries, iterableWithSize(7));
        results.forEach(entry -> assertThat(entries.indexOf(entry), lessThanOrEqualTo(4)));
    }

    @ParameterizedTest(name = "[{index}][{1}] Using 'AND' key word with simple comparison.")
    @MethodSource("argumentsSortingForANDKeyLevelOneProvider")
    @Order(3)
    public void shouldSortNamesWhenUsingConjunctionSimpleComparison(final List<String> entries, final String bddKeyOne,
                                                             final List<String> results) {
        // Arrange
        final Comparator<String> sut = new DisplayNameComparator(bddKeyOne);

        // Act
        entries.sort(sut);

        // Assert
        assertThat(entries, iterableWithSize(9));
        results.forEach(entry -> {
            final String reason = String.format("Invalid position for entry [%s] in %s", entry, entries);
            if (entry.toLowerCase().startsWith(bddKeyOne)) {
                assertThat(reason, entries.indexOf(entry), lessThanOrEqualTo(1));
            } else if (entry.toLowerCase().startsWith("and " + bddKeyOne)) {
                assertThat(reason, entries.indexOf(entry), greaterThanOrEqualTo(2));
            } else {
                assertThat(reason, entries.indexOf(entry), greaterThanOrEqualTo(3));
            }
        });
    }

    @ParameterizedTest(name = "[{index}][{1}] Using 'AND' key word with complex comparison.")
    @MethodSource("argumentsSortingForANDKeyLevelTwoProvider")
    @Order(4)
    public void shouldSortNamesWhenUsingConjunctionComplexComparison(final List<String> entries, final String bddKeyOne,
                                                              final String bddKeyTwo, final List<String> results) {
        // Arrange
        final Comparator<String> sut = new DisplayNameComparator(bddKeyOne)
                .thenComparing(new DisplayNameComparator(bddKeyTwo));

        // Act
        entries.sort(sut);

        // Assert
        assertThat(entries, iterableWithSize(9));
        assertThat(entries, iterableWithSize(9));
        results.forEach(entry -> {
            final String reason = String.format("Invalid position for entry [%s] in %s", entry, entries);
            if (entry.toLowerCase().startsWith(bddKeyOne)) {
                assertThat(reason, entries.indexOf(entry), lessThanOrEqualTo(1));
            } else if (entry.toLowerCase().startsWith("and " + bddKeyOne)) {
                assertThat(reason, entries.indexOf(entry), greaterThanOrEqualTo(2));
            } else {
                assertThat(reason, entries.indexOf(entry), greaterThanOrEqualTo(3));
            }
        });
    }

    private static Stream<Arguments> argumentsSortingLevelOneProvider() {
        final List<String> words = new ArrayList<>();
        words.add(THEN_CITIZEN_HOUSE_NOT_CHARGE);
        words.add(WHEN_THE_CITIZEN_IS_WORKING_HARD);
        words.add(THEN_CITIZEN_KIDS_ARE_HAPPY);
        words.add(GIVEN_SOMEONE_PETS);
        words.add(WHEN_WE_CALCULATE_TAXES);
        words.add(GIVEN_CITIZEN_CAR);

        return Stream.of(
                Arguments.of(words, GIVEN, List.of(GIVEN_SOMEONE_PETS, GIVEN_CITIZEN_CAR)),
                Arguments.of(words, WHEN, List.of(WHEN_THE_CITIZEN_IS_WORKING_HARD, WHEN_WE_CALCULATE_TAXES)),
                Arguments.of(words, THEN, List.of(THEN_CITIZEN_HOUSE_NOT_CHARGE, THEN_CITIZEN_KIDS_ARE_HAPPY))
        );
    }

    private static Stream<Arguments> argumentsSortingLevelTwoProvider() {
        final List<String> words = new ArrayList<>();
        words.add(THEN_CITIZEN_HOUSE_NOT_CHARGE);
        words.add(WHEN_THE_CITIZEN_IS_WORKING_HARD);
        words.add(AND_GIVEN_SOMEONE_PETS);
        words.add(GIVEN_SOMEONE_PETS);
        words.add(AND_WHEN_THE_CITIZEN_START_B2B);
        words.add(WHEN_WE_CALCULATE_TAXES);
        words.add(GIVEN_CITIZEN_CAR);

        return Stream.of(
                Arguments.of(words, GIVEN, WHEN, List.of(GIVEN_SOMEONE_PETS, GIVEN_CITIZEN_CAR, WHEN_THE_CITIZEN_IS_WORKING_HARD, WHEN_WE_CALCULATE_TAXES)),
                Arguments.of(words, WHEN, THEN, List.of(WHEN_THE_CITIZEN_IS_WORKING_HARD, WHEN_WE_CALCULATE_TAXES, THEN_CITIZEN_HOUSE_NOT_CHARGE, THEN_CITIZEN_KIDS_ARE_HAPPY)),
                Arguments.of(words, GIVEN, THEN, List.of(GIVEN_SOMEONE_PETS, GIVEN_CITIZEN_CAR, THEN_CITIZEN_HOUSE_NOT_CHARGE, THEN_CITIZEN_KIDS_ARE_HAPPY))
        );
    }

    private static Stream<Arguments> argumentsSortingForANDKeyLevelOneProvider() {
        final List<String> words = new ArrayList<>();
        words.add(THEN_CITIZEN_HOUSE_NOT_CHARGE);
        words.add(WHEN_THE_CITIZEN_IS_WORKING_HARD);
        words.add(WHEN_WE_CALCULATE_TAXES);
        words.add(AND_GIVEN_SOMEONE_PETS);
        words.add(GIVEN_SOMEONE_PETS);
        words.add(THEN_CITIZEN_KIDS_ARE_HAPPY);
        words.add(AND_THEN_CITIZEN_PARENTS_ARE_HAPPY);
        words.add(AND_WHEN_THE_CITIZEN_START_B2B);
        words.add(GIVEN_CITIZEN_CAR);

        return Stream.of(
                Arguments.of(words, GIVEN, List.of(GIVEN_SOMEONE_PETS, GIVEN_CITIZEN_CAR, AND_THEN_CITIZEN_PARENTS_ARE_HAPPY)),
                Arguments.of(words, WHEN, List.of(WHEN_THE_CITIZEN_IS_WORKING_HARD, WHEN_WE_CALCULATE_TAXES, AND_WHEN_THE_CITIZEN_START_B2B)),
                Arguments.of(words, THEN, List.of(AND_THEN_CITIZEN_PARENTS_ARE_HAPPY, THEN_CITIZEN_HOUSE_NOT_CHARGE, THEN_CITIZEN_KIDS_ARE_HAPPY))
        );
    }

    private static Stream<Arguments> argumentsSortingForANDKeyLevelTwoProvider() {
        final List<String> words = new ArrayList<>();
        words.add(THEN_CITIZEN_HOUSE_NOT_CHARGE);
        words.add(WHEN_THE_CITIZEN_IS_WORKING_HARD);
        words.add(WHEN_WE_CALCULATE_TAXES);
        words.add(AND_GIVEN_SOMEONE_PETS);
        words.add(GIVEN_SOMEONE_PETS);
        words.add(THEN_CITIZEN_KIDS_ARE_HAPPY);
        words.add(AND_THEN_CITIZEN_PARENTS_ARE_HAPPY);
        words.add(AND_WHEN_THE_CITIZEN_START_B2B);
        words.add(GIVEN_CITIZEN_CAR);

        return Stream.of(
                Arguments.of(words, GIVEN, WHEN, List.of(GIVEN_SOMEONE_PETS, GIVEN_CITIZEN_CAR, AND_GIVEN_SOMEONE_PETS)),
                Arguments.of(words, WHEN, THEN, List.of(WHEN_THE_CITIZEN_IS_WORKING_HARD, WHEN_WE_CALCULATE_TAXES, AND_WHEN_THE_CITIZEN_START_B2B)),
                Arguments.of(words, GIVEN, THEN, List.of(THEN_CITIZEN_KIDS_ARE_HAPPY, THEN_CITIZEN_HOUSE_NOT_CHARGE, AND_THEN_CITIZEN_PARENTS_ARE_HAPPY))
        );
    }

}