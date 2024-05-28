package lu.mms.common.quality.junit.assets.db.re;

public interface Operator<T> extends Statement {

    Operator<T> and(final Statement statement);

    Operator<T> or(final Statement statement);

    Operator<T> not(final Statement statement);

}
