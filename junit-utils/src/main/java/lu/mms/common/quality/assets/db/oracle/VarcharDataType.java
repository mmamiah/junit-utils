package lu.mms.common.quality.assets.db.oracle;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public enum VarcharDataType {

    /**
     * <b>Range: </b> {@code 1 <= p <= 255}
     * <b>Oracle Type</b> CHAR(s)
     */
    CHAR("CHAR(s)", "CHARACTER", Map.entry(1, 255)),

    /**
     * <b>Range: </b> {@code 1 <= p <= 32767}
     * <b>Oracle Type</b> VARCHAR2(s)
     */
    VARCHAR("VARCHAR2(s)", "CHARACTER VARYING", Map.entry(1, 32767)),

    /**
     * <b>Range: </b> {@code 1 <= p <= 32767}
     * <b>Oracle Type</b> VARCHAR2(s)
     */
    VARCHAR2("VARCHAR2(s)", "CHARACTER VARYING", Map.entry(1, 32767));

    private final String format;
    private final String description;
    private final Map.Entry<Integer, Integer> length;

    VarcharDataType(final String format, final String description, final Map.Entry<Integer, Integer> length) {
        this.format = format;
        this.description = description;
        this.length = length;
    }

    public String getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isCharacterString(final String type) {
        return Stream.of(VarcharDataType.values())
                .anyMatch(value -> value.name().equals(type));
    }

    public static String format(final String type, final Integer length) {
        return Stream.of(VarcharDataType.values())
                .filter(value -> value.name().equals(type))
                .findFirst()
                .or(() -> Optional.ofNullable(VarcharDataType.of(type)))
                .map(value -> StringUtils.replace(value.getFormat(), "s", length.toString()))
                .orElse(StringUtils.EMPTY);
    }

    public static VarcharDataType of(final String description) {
        return Stream.of(VarcharDataType.values())
                .filter(value -> value.getDescription().equals(description))
                .findFirst()
                .orElse(null);
    }

}
