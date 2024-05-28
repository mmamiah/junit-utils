package lu.mms.common.quality.assets.db.oracle;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Stream;

public enum NumberDataType {

    /**
     * <b>Oracle Type</b> NUMBER
     */
    NUMBER("NUMBER"),

    /**
     * <b>Oracle Type</b> NUMBER(10)
     */
    INTEGER("NUMBER(p)", Map.entry(0, 10)),

    /**
     * <b>Oracle Type</b> NUMBER(5)
     */
    SMALLINT("NUMBER(p)", Map.entry(0, 5)),

    /**
     * <b>Oracle Type</b> NUMBER(19)
     */
    BIGINT("NUMBER(p)", Map.entry(6, 19)),

    /**
     * <b>Oracle Type</b> NUMBER
     */
    FLOAT("NUMBER", Map.entry(54, 126)),

    /**
     * <b>Range</b> {@code 1 <= p <= 31, 1 <= s <= 31}
     * <b>Oracle Type</b> NUMBER(p,s)
     */
    DECIMAL("NUMBER(p,s)", Map.entry(1, 31), Map.entry(1, 31)),

    /**
     * <b>Range</b> {@code 1 <= p <= 31, 1 <= s <= 31}
     * <b>Oracle Type</b> NUMBER(p,s)
     */
    NUMERIC("NUMBER(p,s)", Map.entry(1, 31), Map.entry(1, 31)),

    /**
     * <b>Oracle Type</b> NUMBER
     */
    DECFLOAT("NUMBER", Map.entry(54, 126));

    private final String format;
    private final Map.Entry<Integer, Integer> precision;
    private final Map.Entry<Integer, Integer> scale;

    NumberDataType(final String format) {
        this(format, null, null);
    }

    NumberDataType(final String format, final Map.Entry<Integer, Integer> precision) {
        this(format, precision, null);
    }

    NumberDataType(final String format, final Map.Entry<Integer, Integer> precision, final Map.Entry<Integer, Integer> scale) {
        this.format = format;
        this.precision = precision;
        this.scale = scale;
    }

    public String getFormat() {
        return format;
    }

    public Map.Entry<Integer, Integer> getPrecision() {
        return precision;
    }

    public Map.Entry<Integer, Integer> getScale() {
        return scale;
    }

    public static boolean isNumeric(final String type) {
        return Stream.of(NumberDataType.values())
                .anyMatch(value -> value.name().equals(type));
    }

    public static String format(final Integer precision, final Integer scale) {
        return Stream.of(NumberDataType.values())
                .filter(type -> isValidPrecision(type, precision) && isValidScale(type, scale))
                .findFirst()
                .map(value -> {
                    final String formatted;
                    if (value.getFormat().contains("p")) {
                        formatted = StringUtils.replace(value.getFormat(), "p", precision.toString());
                    } else {
                        formatted = value.getFormat();
                    }
                    if (formatted.contains("s")) {
                        return StringUtils.replace(formatted, "s", scale.toString());
                    }
                    return  formatted;
                })
                .orElse(StringUtils.EMPTY);
    }

    private static boolean isValidPrecision(final NumberDataType dataType, final Integer precision) {
        if (precision == null || dataType.getPrecision() == null) {
            return precision == null && dataType.getPrecision() == null;
        }
        final Map.Entry<Integer, Integer> range = dataType.getPrecision();
        return range.getKey() <= precision && range.getValue() >= precision;
    }

    private static boolean isValidScale(final NumberDataType dataType, final Integer scale) {
        final boolean isScale = ObjectUtils.defaultIfNull(scale, 0) > 0 && dataType.getFormat().contains("s");
        final Map.Entry<Integer, Integer> range = dataType.getScale();
        return (isScale && (range.getKey() <= scale && range.getValue() >= scale)) || range == null;
    }
}
