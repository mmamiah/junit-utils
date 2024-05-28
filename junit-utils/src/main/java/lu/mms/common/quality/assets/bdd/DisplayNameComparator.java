package lu.mms.common.quality.assets.bdd;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * Comparing the display name as per the given key (GIVEN, WHEN, THEN).
 */
class DisplayNameComparator implements Comparator<String> {

    private static final String AND = "and";

    private final String bddKey;

    DisplayNameComparator(final String bddKey) {
        this.bddKey = bddKey;
    }

    @Override
    public int compare(final String descOne, final String descTwo) {
        return getComparatorByKey(bddKey)
                .thenComparing(getComparatorByKey(AND + bddKey))
                .compare(descOne, descTwo);
    }

    private static Comparator<String> getComparatorByKey(final String key) {
        return (descOne, descTwo) -> {
            if (isMatchingTemplate(key, getCleanEntry(descOne))) {
                return -1;
            } else if (isMatchingTemplate(key, getCleanEntry(descTwo))) {
                return 1;
            }
            return 0;
        };
    }

    /**
     * Remove extra space in a given string. <br>
     * e.g.: " hello     there   " will return "hello there"
     * @param key The string to clean
     * @return  The cleaned string object
     */
    private static String getCleanEntry(final String key) {
        return key.trim().replaceAll(" ", StringUtils.EMPTY);
    }

    private static boolean isMatchingTemplate(final String template, final String methodName) {
        return methodName.toLowerCase().startsWith(template.toLowerCase());
    }

}
