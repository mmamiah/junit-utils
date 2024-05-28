package lu.mms.common.quality.junit.assets.db.re;

public interface Condition<T> extends Statement {

    /**
     * The SQL 'Equal' comparison.
     *
     * @param value      the value
     * @return the sql condition
     */
    Operator<T> eq(final Object value);

    /**
     * The SQL 'Greater Than' comparison.
     *
     * @param value      the value
     * @return the sql comparison
     */
    Operator<T> gt(final Object value) ;

    /**
     * The SQL 'Greater Than or Equal' comparison.
     *
     * @param value      the value
     * @return the sql comparison
     */
    Operator<T> ge(final Object value);

    /**
     * The SQL 'Less Than' comparison.
     *
     * @param value      the value
     * @return the sql comparison
     */
    Operator<T> lt(final Object value);

    /**
     * The SQL 'Less Than or Equal' comparison.
     *
     * @param value      the value
     * @return the sql comparison
     */
    Operator<T> le(final Object value);

    /**
     * The SQL 'Not Equal' comparison.
     *
     * @param value      the value
     * @return the sql comparison
     */
    Operator<T> not(final Object value);

    /**
     * The SQL 'Between' comparison.
     *
     * @param from  the value #1
     * @param to    the value #2
     * @return the sql comparison
     */
    Operator<T> between(final Object from, final Object to);

    /**
     * The SQL 'LIKE' comparison.
     *
     * @param value      the value
     * @return the sql comparison
     */
    Operator<T> like(final Object value);

    /**
     * The SQL 'IN' comparison.
     *
     * @param values     the values
     * @return the sql comparison
     */
    Operator<T> in(final Object... values);

}
