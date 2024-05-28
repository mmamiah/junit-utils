package lu.mms.common.quality.junit.assets.db.hsql;

public class DefaultSqlRoutines {

    public static final String ROUTINE_DIGIT = " " +
            "CREATE FUNCTION digits (number VARCHAR(30))\n" +
            "   RETURNS INTEGER\n" +
            "   RETURN CAST(number AS INT)";

    @InvokedRoutine
    public static String digits(final String value) {
        return value;
    }
}
